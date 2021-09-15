package org.springframework.boot.actuate.netty.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.netty.http.HttpRequestHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author thinking
 * @version 1.0
 * @since 2/5/21
 */
//@Controller
//@RequestMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
public class InfoEndpoint implements HttpRequestHandler<Object> {

    List<InfoContributor> infoContributors;

    public InfoEndpoint(ObjectProvider<List<InfoContributor>> infoContributors) {
        this.infoContributors = infoContributors.getIfAvailable();
        if (this.infoContributors == null) {
            this.infoContributors = Collections.<InfoContributor>emptyList();
        }
    }

    @Override
    public <R> R handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, Object inputBody) throws IOException {
        Info.Builder builder = new Info.Builder();
        for (InfoContributor contributor : this.infoContributors) {
            contributor.contribute(builder);
        }
        Info build = builder.build();
        return (R) build.getDetails();
    }
}
