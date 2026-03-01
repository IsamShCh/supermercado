package com.isam.simulador.client;

import io.grpc.ClientInterceptor;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Utility class for gRPC metadata operations.
 */
public class MetadataUtils {

    /**
     * ClientInterceptor that adds fixed headers to outgoing gRPC calls.
     */
    public static class FixedHeadersInterceptor implements ClientInterceptor {
        private final Metadata extraHeaders;

        public FixedHeadersInterceptor(Metadata extraHeaders) {
            this.extraHeaders = extraHeaders;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    headers.merge(extraHeaders);
                    super.start(responseListener, headers);
                }
            };
        }
    }
}
