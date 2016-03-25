package com.fxiaoke.dataplatform.flume.ng.util;

import com.github.autoconf.ConfigFactory;
import com.github.autoconf.api.IChangeListener;
import com.github.autoconf.api.IConfig;
import com.github.autoconf.api.IConfigFactory;
import com.github.trace.TraceContext;
import com.github.trace.TraceRecorder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.Reflection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

import static com.github.mongo.support.util.InternUtil.*;


public class ConfigurableMongoFactoryBean<T> implements InitializingBean, DisposableBean, FactoryBean<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private IConfigFactory configFactory;
    private String configName;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    private String dbName;
    private String mapName;
    private String addressInfo;
    private volatile MongoClient mongo;
    private Set<String> names = ImmutableSet.of("hashCode", "toString", "equals");
    private Class<T> clazz;

    @SuppressWarnings("unchecked")
    public ConfigurableMongoFactoryBean() {
        this.clazz =
            (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public IConfigFactory getConfigFactory() {
        return configFactory;
    }

    public void setConfigFactory(IConfigFactory configFactory) {
        this.configFactory = configFactory;
    }



    protected Datastore getDataStore() {
        Preconditions.checkNotNull(dbName, "the dbName shouldn't be null!");
        Morphia morphia = new Morphia();
        morphia.mapPackage(mapName);
        return morphia.createDatastore(mongo, dbName);
    }

    private void loadConfig(IConfig config) {
        dbName = config.get("mongo.dbName");
        mapName = config.get("mongo.mapPackage");
        addressInfo = config.get("mongo.servers");
        Preconditions.checkNotNull(addressInfo, "The servers configuration is incorrect!");

        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.socketKeepAlive(true).connectTimeout(config.getInt("mongo.connectTimeout", 5000)).socketTimeout(config.getInt("mongo.socketTimeout", 10000));
        MongoClient client = new MongoClient(new MongoClientURI(addressInfo, builder));
        if (mongo != null) {
            MongoClient old = mongo;
            mongo = client;
            old.close();
        } else {
            mongo = client;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (configFactory == null) {
            configFactory = ConfigFactory.getInstance();
        }
        configFactory.getConfig(configName, new IChangeListener() {
            @Override
            public void changed(IConfig config) {
                try {
                    loadConfig(config);
                } catch (Exception e) {
                    log.error("cannot load: {}", config.getName(), e);
                }
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        if (mongo != null) {
            mongo.close();
        }
    }

    @Override
    public Class<T> getObjectType() {
        return clazz;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return Reflection.newProxy(clazz, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (names.contains(method.getName())) {
                    return method.invoke(proxy, args);
                }
                return handle(method, args);
            }

            private Object handle(Method method, Object[] args) throws Throwable {
                TraceContext context = TraceContext.get();
                context.reset();
                context.inc();
                context.setIface(dbName);
                context.setMethod(method.getName());
                context.setParameter(getParameters(args));
                context.setServerName(configName);
                context.setUrl(addressInfo);
                long start = System.currentTimeMillis();
                try {
                    return method.invoke(getDataStore(), args);
                } catch (Exception e) {
                    context.setFail(true);
                    context.setReason(getFirstNotNullMessage(e));
                    throw e;
                } finally {
                    context.setStamp(start);
                    context.setCost(System.currentTimeMillis() - start);
                    TraceRecorder.getInstance().post(context.copy());
                }
            }
        });
    }
}
