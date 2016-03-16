package com.fxiaoke.dataplatform.flume.ng.util.proto;

public final class Offset {
    private Offset() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
    }

    public interface dataSetOrBuilder
            extends com.google.protobuf.MessageOrBuilder {

        // repeated .flume.offsetKv kmap = 1;

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        java.util.List<offsetKv>
        getKmapList();

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        offsetKv getKmap(int index);

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        int getKmapCount();

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        java.util.List<? extends offsetKvOrBuilder>
        getKmapOrBuilderList();

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        offsetKvOrBuilder getKmapOrBuilder(
                int index);

        // optional bool isRotate = 2;

        /**
         * <code>optional bool isRotate = 2;</code>
         */
        boolean hasIsRotate();

        /**
         * <code>optional bool isRotate = 2;</code>
         */
        boolean getIsRotate();
    }

    /**
     * Protobuf type {@code flume.dataSet}
     */
    public static final class dataSet extends
            com.google.protobuf.GeneratedMessage
            implements dataSetOrBuilder {
        // Use dataSet.newBuilder() to construct.
        private dataSet(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private dataSet(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final dataSet defaultInstance;

        public static dataSet getDefaultInstance() {
            return defaultInstance;
        }

        public dataSet getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }

        private dataSet(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields,
                                    extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 10: {
                            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                                kmap_ = new java.util.ArrayList<offsetKv>();
                                mutable_bitField0_ |= 0x00000001;
                            }
                            kmap_.add(input.readMessage(offsetKv.PARSER, extensionRegistry));
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000001;
                            isRotate_ = input.readBool();
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e.getMessage()).setUnfinishedMessage(this);
            } finally {
                if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                    kmap_ = java.util.Collections.unmodifiableList(kmap_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return Offset.internal_static_flume_dataSet_descriptor;
        }

        protected FieldAccessorTable
        internalGetFieldAccessorTable() {
            return Offset.internal_static_flume_dataSet_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            dataSet.class, Builder.class);
        }

        public static com.google.protobuf.Parser<dataSet> PARSER =
                new com.google.protobuf.AbstractParser<dataSet>() {
                    public dataSet parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws com.google.protobuf.InvalidProtocolBufferException {
                        return new dataSet(input, extensionRegistry);
                    }
                };

        @Override
        public com.google.protobuf.Parser<dataSet> getParserForType() {
            return PARSER;
        }

        private int bitField0_;
        // repeated .flume.offsetKv kmap = 1;
        public static final int KMAP_FIELD_NUMBER = 1;
        private java.util.List<offsetKv> kmap_;

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        public java.util.List<offsetKv> getKmapList() {
            return kmap_;
        }

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        public java.util.List<? extends offsetKvOrBuilder>
        getKmapOrBuilderList() {
            return kmap_;
        }

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        public int getKmapCount() {
            return kmap_.size();
        }

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        public offsetKv getKmap(int index) {
            return kmap_.get(index);
        }

        /**
         * <code>repeated .flume.offsetKv kmap = 1;</code>
         */
        public offsetKvOrBuilder getKmapOrBuilder(
                int index) {
            return kmap_.get(index);
        }

        // optional bool isRotate = 2;
        public static final int ISROTATE_FIELD_NUMBER = 2;
        private boolean isRotate_;

        /**
         * <code>optional bool isRotate = 2;</code>
         */
        public boolean hasIsRotate() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        /**
         * <code>optional bool isRotate = 2;</code>
         */
        public boolean getIsRotate() {
            return isRotate_;
        }

        private void initFields() {
            kmap_ = java.util.Collections.emptyList();
            isRotate_ = false;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1) return isInitialized == 1;

            for (int i = 0; i < getKmapCount(); i++) {
                if (!getKmap(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            getSerializedSize();
            for (int i = 0; i < kmap_.size(); i++) {
                output.writeMessage(1, kmap_.get(i));
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBool(2, isRotate_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;

            size = 0;
            for (int i = 0; i < kmap_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(1, kmap_.get(i));
            }
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBoolSize(2, isRotate_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @Override
        protected Object writeReplace()
                throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        public static dataSet parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static dataSet parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static dataSet parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static dataSet parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static dataSet parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static dataSet parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static dataSet parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static dataSet parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static dataSet parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static dataSet parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(dataSet prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @Override
        protected Builder newBuilderForType(
                BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        /**
         * Protobuf type {@code flume.dataSet}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder>
                implements dataSetOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return Offset.internal_static_flume_dataSet_descriptor;
            }

            protected FieldAccessorTable
            internalGetFieldAccessorTable() {
                return Offset.internal_static_flume_dataSet_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                dataSet.class, Builder.class);
            }

            // Construct using com.fxiaoke.dataplatform.flume.ng.util.proto.Offset.dataSet.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getKmapFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                if (kmapBuilder_ == null) {
                    kmap_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                } else {
                    kmapBuilder_.clear();
                }
                isRotate_ = false;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return Offset.internal_static_flume_dataSet_descriptor;
            }

            public dataSet getDefaultInstanceForType() {
                return dataSet.getDefaultInstance();
            }

            public dataSet build() {
                dataSet result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public dataSet buildPartial() {
                dataSet result = new dataSet(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (kmapBuilder_ == null) {
                    if (((bitField0_ & 0x00000001) == 0x00000001)) {
                        kmap_ = java.util.Collections.unmodifiableList(kmap_);
                        bitField0_ = (bitField0_ & ~0x00000001);
                    }
                    result.kmap_ = kmap_;
                } else {
                    result.kmap_ = kmapBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.isRotate_ = isRotate_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof dataSet) {
                    return mergeFrom((dataSet) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(dataSet other) {
                if (other == dataSet.getDefaultInstance()) return this;
                if (kmapBuilder_ == null) {
                    if (!other.kmap_.isEmpty()) {
                        if (kmap_.isEmpty()) {
                            kmap_ = other.kmap_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                        } else {
                            ensureKmapIsMutable();
                            kmap_.addAll(other.kmap_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.kmap_.isEmpty()) {
                        if (kmapBuilder_.isEmpty()) {
                            kmapBuilder_.dispose();
                            kmapBuilder_ = null;
                            kmap_ = other.kmap_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                            kmapBuilder_ =
                                    com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                                            getKmapFieldBuilder() : null;
                        } else {
                            kmapBuilder_.addAllMessages(other.kmap_);
                        }
                    }
                }
                if (other.hasIsRotate()) {
                    setIsRotate(other.getIsRotate());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                for (int i = 0; i < getKmapCount(); i++) {
                    if (!getKmap(i).isInitialized()) {

                        return false;
                    }
                }
                return true;
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                dataSet parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (dataSet) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            // repeated .flume.offsetKv kmap = 1;
            private java.util.List<offsetKv> kmap_ =
                    java.util.Collections.emptyList();

            private void ensureKmapIsMutable() {
                if (!((bitField0_ & 0x00000001) == 0x00000001)) {
                    kmap_ = new java.util.ArrayList<offsetKv>(kmap_);
                    bitField0_ |= 0x00000001;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<
                    offsetKv, offsetKv.Builder, offsetKvOrBuilder> kmapBuilder_;

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public java.util.List<offsetKv> getKmapList() {
                if (kmapBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(kmap_);
                } else {
                    return kmapBuilder_.getMessageList();
                }
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public int getKmapCount() {
                if (kmapBuilder_ == null) {
                    return kmap_.size();
                } else {
                    return kmapBuilder_.getCount();
                }
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public offsetKv getKmap(int index) {
                if (kmapBuilder_ == null) {
                    return kmap_.get(index);
                } else {
                    return kmapBuilder_.getMessage(index);
                }
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder setKmap(
                    int index, offsetKv value) {
                if (kmapBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureKmapIsMutable();
                    kmap_.set(index, value);
                    onChanged();
                } else {
                    kmapBuilder_.setMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder setKmap(
                    int index, offsetKv.Builder builderForValue) {
                if (kmapBuilder_ == null) {
                    ensureKmapIsMutable();
                    kmap_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    kmapBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder addKmap(offsetKv value) {
                if (kmapBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureKmapIsMutable();
                    kmap_.add(value);
                    onChanged();
                } else {
                    kmapBuilder_.addMessage(value);
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder addKmap(
                    int index, offsetKv value) {
                if (kmapBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureKmapIsMutable();
                    kmap_.add(index, value);
                    onChanged();
                } else {
                    kmapBuilder_.addMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder addKmap(
                    offsetKv.Builder builderForValue) {
                if (kmapBuilder_ == null) {
                    ensureKmapIsMutable();
                    kmap_.add(builderForValue.build());
                    onChanged();
                } else {
                    kmapBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder addKmap(
                    int index, offsetKv.Builder builderForValue) {
                if (kmapBuilder_ == null) {
                    ensureKmapIsMutable();
                    kmap_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    kmapBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder addAllKmap(
                    Iterable<? extends offsetKv> values) {
                if (kmapBuilder_ == null) {
                    ensureKmapIsMutable();
                    super.addAll(values, kmap_);
                    onChanged();
                } else {
                    kmapBuilder_.addAllMessages(values);
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder clearKmap() {
                if (kmapBuilder_ == null) {
                    kmap_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                    onChanged();
                } else {
                    kmapBuilder_.clear();
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public Builder removeKmap(int index) {
                if (kmapBuilder_ == null) {
                    ensureKmapIsMutable();
                    kmap_.remove(index);
                    onChanged();
                } else {
                    kmapBuilder_.remove(index);
                }
                return this;
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public offsetKv.Builder getKmapBuilder(
                    int index) {
                return getKmapFieldBuilder().getBuilder(index);
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public offsetKvOrBuilder getKmapOrBuilder(
                    int index) {
                if (kmapBuilder_ == null) {
                    return kmap_.get(index);
                } else {
                    return kmapBuilder_.getMessageOrBuilder(index);
                }
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public java.util.List<? extends offsetKvOrBuilder>
            getKmapOrBuilderList() {
                if (kmapBuilder_ != null) {
                    return kmapBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(kmap_);
                }
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public offsetKv.Builder addKmapBuilder() {
                return getKmapFieldBuilder().addBuilder(
                        offsetKv.getDefaultInstance());
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public offsetKv.Builder addKmapBuilder(
                    int index) {
                return getKmapFieldBuilder().addBuilder(
                        index, offsetKv.getDefaultInstance());
            }

            /**
             * <code>repeated .flume.offsetKv kmap = 1;</code>
             */
            public java.util.List<offsetKv.Builder>
            getKmapBuilderList() {
                return getKmapFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<
                    offsetKv, offsetKv.Builder, offsetKvOrBuilder>
            getKmapFieldBuilder() {
                if (kmapBuilder_ == null) {
                    kmapBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                            offsetKv, offsetKv.Builder, offsetKvOrBuilder>(
                            kmap_,
                            ((bitField0_ & 0x00000001) == 0x00000001),
                            getParentForChildren(),
                            isClean());
                    kmap_ = null;
                }
                return kmapBuilder_;
            }

            // optional bool isRotate = 2;
            private boolean isRotate_;

            /**
             * <code>optional bool isRotate = 2;</code>
             */
            public boolean hasIsRotate() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            /**
             * <code>optional bool isRotate = 2;</code>
             */
            public boolean getIsRotate() {
                return isRotate_;
            }

            /**
             * <code>optional bool isRotate = 2;</code>
             */
            public Builder setIsRotate(boolean value) {
                bitField0_ |= 0x00000002;
                isRotate_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>optional bool isRotate = 2;</code>
             */
            public Builder clearIsRotate() {
                bitField0_ = (bitField0_ & ~0x00000002);
                isRotate_ = false;
                onChanged();
                return this;
            }

            // @@protoc_insertion_point(builder_scope:flume.dataSet)
        }

        static {
            defaultInstance = new dataSet(true);
            defaultInstance.initFields();
        }

        // @@protoc_insertion_point(class_scope:flume.dataSet)
    }

    public interface offsetKvOrBuilder
            extends com.google.protobuf.MessageOrBuilder {

        // required string filename = 1;

        /**
         * <code>required string filename = 1;</code>
         */
        boolean hasFilename();

        /**
         * <code>required string filename = 1;</code>
         */
        String getFilename();

        /**
         * <code>required string filename = 1;</code>
         */
        com.google.protobuf.ByteString
        getFilenameBytes();

        // required sfixed64 offset = 2;

        /**
         * <code>required sfixed64 offset = 2;</code>
         */
        boolean hasOffset();

        /**
         * <code>required sfixed64 offset = 2;</code>
         */
        long getOffset();

        // required sfixed64 lineNumber = 3;

        /**
         * <code>required sfixed64 lineNumber = 3;</code>
         */
        boolean hasLineNumber();

        /**
         * <code>required sfixed64 lineNumber = 3;</code>
         */
        long getLineNumber();
    }

    /**
     * Protobuf type {@code flume.offsetKv}
     */
    public static final class offsetKv extends
            com.google.protobuf.GeneratedMessage
            implements offsetKvOrBuilder {
        // Use offsetKv.newBuilder() to construct.
        private offsetKv(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private offsetKv(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final offsetKv defaultInstance;

        public static offsetKv getDefaultInstance() {
            return defaultInstance;
        }

        public offsetKv getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }

        private offsetKv(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields,
                                    extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 10: {
                            bitField0_ |= 0x00000001;
                            filename_ = input.readBytes();
                            break;
                        }
                        case 17: {
                            bitField0_ |= 0x00000002;
                            offset_ = input.readSFixed64();
                            break;
                        }
                        case 25: {
                            bitField0_ |= 0x00000004;
                            lineNumber_ = input.readSFixed64();
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return Offset.internal_static_flume_offsetKv_descriptor;
        }

        protected FieldAccessorTable
        internalGetFieldAccessorTable() {
            return Offset.internal_static_flume_offsetKv_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            offsetKv.class, Builder.class);
        }

        public static com.google.protobuf.Parser<offsetKv> PARSER =
                new com.google.protobuf.AbstractParser<offsetKv>() {
                    public offsetKv parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws com.google.protobuf.InvalidProtocolBufferException {
                        return new offsetKv(input, extensionRegistry);
                    }
                };

        @Override
        public com.google.protobuf.Parser<offsetKv> getParserForType() {
            return PARSER;
        }

        private int bitField0_;
        // required string filename = 1;
        public static final int FILENAME_FIELD_NUMBER = 1;
        private Object filename_;

        /**
         * <code>required string filename = 1;</code>
         */
        public boolean hasFilename() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        /**
         * <code>required string filename = 1;</code>
         */
        public String getFilename() {
            Object ref = filename_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    filename_ = s;
                }
                return s;
            }
        }

        /**
         * <code>required string filename = 1;</code>
         */
        public com.google.protobuf.ByteString
        getFilenameBytes() {
            Object ref = filename_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (String) ref);
                filename_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        // required sfixed64 offset = 2;
        public static final int OFFSET_FIELD_NUMBER = 2;
        private long offset_;

        /**
         * <code>required sfixed64 offset = 2;</code>
         */
        public boolean hasOffset() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        /**
         * <code>required sfixed64 offset = 2;</code>
         */
        public long getOffset() {
            return offset_;
        }

        // required sfixed64 lineNumber = 3;
        public static final int LINENUMBER_FIELD_NUMBER = 3;
        private long lineNumber_;

        /**
         * <code>required sfixed64 lineNumber = 3;</code>
         */
        public boolean hasLineNumber() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        /**
         * <code>required sfixed64 lineNumber = 3;</code>
         */
        public long getLineNumber() {
            return lineNumber_;
        }

        private void initFields() {
            filename_ = "";
            offset_ = 0L;
            lineNumber_ = 0L;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1) return isInitialized == 1;

            if (!hasFilename()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasOffset()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasLineNumber()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getFilenameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeSFixed64(2, offset_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeSFixed64(3, lineNumber_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;

            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(1, getFilenameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeSFixed64Size(2, offset_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeSFixed64Size(3, lineNumber_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @Override
        protected Object writeReplace()
                throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        public static offsetKv parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static offsetKv parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static offsetKv parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static offsetKv parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static offsetKv parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static offsetKv parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static offsetKv parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static offsetKv parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static offsetKv parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static offsetKv parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(offsetKv prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @Override
        protected Builder newBuilderForType(
                BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        /**
         * Protobuf type {@code flume.offsetKv}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder>
                implements offsetKvOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return Offset.internal_static_flume_offsetKv_descriptor;
            }

            protected FieldAccessorTable
            internalGetFieldAccessorTable() {
                return Offset.internal_static_flume_offsetKv_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                offsetKv.class, Builder.class);
            }

            // Construct using com.fxiaoke.dataplatform.flume.ng.util.proto.Offset.offsetKv.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                filename_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                offset_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000002);
                lineNumber_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return Offset.internal_static_flume_offsetKv_descriptor;
            }

            public offsetKv getDefaultInstanceForType() {
                return offsetKv.getDefaultInstance();
            }

            public offsetKv build() {
                offsetKv result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public offsetKv buildPartial() {
                offsetKv result = new offsetKv(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.filename_ = filename_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.offset_ = offset_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.lineNumber_ = lineNumber_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof offsetKv) {
                    return mergeFrom((offsetKv) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(offsetKv other) {
                if (other == offsetKv.getDefaultInstance()) return this;
                if (other.hasFilename()) {
                    bitField0_ |= 0x00000001;
                    filename_ = other.filename_;
                    onChanged();
                }
                if (other.hasOffset()) {
                    setOffset(other.getOffset());
                }
                if (other.hasLineNumber()) {
                    setLineNumber(other.getLineNumber());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasFilename()) {

                    return false;
                }
                if (!hasOffset()) {

                    return false;
                }
                if (!hasLineNumber()) {

                    return false;
                }
                return true;
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                offsetKv parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (offsetKv) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            // required string filename = 1;
            private Object filename_ = "";

            /**
             * <code>required string filename = 1;</code>
             */
            public boolean hasFilename() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            /**
             * <code>required string filename = 1;</code>
             */
            public String getFilename() {
                Object ref = filename_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref)
                            .toStringUtf8();
                    filename_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            /**
             * <code>required string filename = 1;</code>
             */
            public com.google.protobuf.ByteString
            getFilenameBytes() {
                Object ref = filename_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (String) ref);
                    filename_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            /**
             * <code>required string filename = 1;</code>
             */
            public Builder setFilename(
                    String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                filename_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required string filename = 1;</code>
             */
            public Builder clearFilename() {
                bitField0_ = (bitField0_ & ~0x00000001);
                filename_ = getDefaultInstance().getFilename();
                onChanged();
                return this;
            }

            /**
             * <code>required string filename = 1;</code>
             */
            public Builder setFilenameBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                filename_ = value;
                onChanged();
                return this;
            }

            // required sfixed64 offset = 2;
            private long offset_;

            /**
             * <code>required sfixed64 offset = 2;</code>
             */
            public boolean hasOffset() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            /**
             * <code>required sfixed64 offset = 2;</code>
             */
            public long getOffset() {
                return offset_;
            }

            /**
             * <code>required sfixed64 offset = 2;</code>
             */
            public Builder setOffset(long value) {
                bitField0_ |= 0x00000002;
                offset_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required sfixed64 offset = 2;</code>
             */
            public Builder clearOffset() {
                bitField0_ = (bitField0_ & ~0x00000002);
                offset_ = 0L;
                onChanged();
                return this;
            }

            // required sfixed64 lineNumber = 3;
            private long lineNumber_;

            /**
             * <code>required sfixed64 lineNumber = 3;</code>
             */
            public boolean hasLineNumber() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            /**
             * <code>required sfixed64 lineNumber = 3;</code>
             */
            public long getLineNumber() {
                return lineNumber_;
            }

            /**
             * <code>required sfixed64 lineNumber = 3;</code>
             */
            public Builder setLineNumber(long value) {
                bitField0_ |= 0x00000004;
                lineNumber_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required sfixed64 lineNumber = 3;</code>
             */
            public Builder clearLineNumber() {
                bitField0_ = (bitField0_ & ~0x00000004);
                lineNumber_ = 0L;
                onChanged();
                return this;
            }

            // @@protoc_insertion_point(builder_scope:flume.offsetKv)
        }

        static {
            defaultInstance = new offsetKv(true);
            defaultInstance.initFields();
        }

        // @@protoc_insertion_point(class_scope:flume.offsetKv)
    }

    private static com.google.protobuf.Descriptors.Descriptor
            internal_static_flume_dataSet_descriptor;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_flume_dataSet_fieldAccessorTable;
    private static com.google.protobuf.Descriptors.Descriptor
            internal_static_flume_offsetKv_descriptor;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_flume_offsetKv_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;

    static {
        String[] descriptorData = {
                "\n\014OffSet.proto\022\005flume\":\n\007dataSet\022\035\n\004kmap" +
                        "\030\001 \003(\0132\017.flume.offsetKv\022\020\n\010isRotate\030\002 \001(" +
                        "\010\"@\n\010offsetKv\022\020\n\010filename\030\001 \002(\t\022\016\n\006offse" +
                        "t\030\002 \002(\020\022\022\n\nlineNumber\030\003 \002(\020B!\n\027com.cloud" +
                        "era.util.protoB\006Offset"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        internal_static_flume_dataSet_descriptor =
                                getDescriptor().getMessageTypes().get(0);
                        internal_static_flume_dataSet_fieldAccessorTable = new
                                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                                internal_static_flume_dataSet_descriptor,
                                new String[]{"Kmap", "IsRotate",});
                        internal_static_flume_offsetKv_descriptor =
                                getDescriptor().getMessageTypes().get(1);
                        internal_static_flume_offsetKv_fieldAccessorTable = new
                                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                                internal_static_flume_offsetKv_descriptor,
                                new String[]{"Filename", "Offset", "LineNumber",});
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        }, assigner);
    }

    // @@protoc_insertion_point(outer_class_scope)
}
