package wiki.chenxun.ace.core.base.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import wiki.chenxun.ace.core.base.support.SpringBeanUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @Description: Created by chenxun on 2017/4/8.
 */
public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter  {


    private final Dispatcher dispatcher;

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

    public HttpServerInboundHandler(){
        dispatcher= SpringBeanUtil.getBean(Dispatcher.class);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
         if(msg instanceof FullHttpRequest){
             FullHttpRequest request=(FullHttpRequest) msg;
             Object response=null;
             try{
                 response=dispatcher.doDispatcher(request);
             }catch (Exception ex){
                 // TODO: 异常处理
             }
             ObjectMapper om=new ObjectMapper();
             String jsonStr=om.writer().writeValueAsString(response);


             sendResponse(ctx,request,jsonStr);
         }

    }

    /**
     * 响应HTTP的请求
     * @param ctx
     * @param req
     * @param jsonStr
     */
    private void sendResponse(ChannelHandlerContext ctx, FullHttpRequest req ,String jsonStr)
    {
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        byte[] jsonByteByte = jsonStr.getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(jsonByteByte));
        response.headers().set(CONTENT_TYPE, "application/json");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // FIXME: 开发调试
        System.err.println(cause.getStackTrace());
        super.exceptionCaught(ctx, cause);
    }
}