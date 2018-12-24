package server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.domain.Session;
import protocol.request.CreateGroupRequestPacket;
import protocol.response.CreateGroupResponsePacket;
import util.IDUtil;
import util.SessionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dingzhaolei
 * @date 2018/12/24 14:56
 **/
public class CreateGroupRequestHandler extends SimpleChannelInboundHandler<CreateGroupRequestPacket> {

    private static final Logger logger = LoggerFactory.getLogger(CreateGroupRequestHandler.class);

    protected void channelRead0(ChannelHandlerContext ctx, CreateGroupRequestPacket msg) throws Exception{
        List<String> userIdList = msg.getUserIdList();

        List<String> userNameList = new ArrayList<>();

        //1.创建一个channel 分组
        ChannelGroup channelGroup =new DefaultChannelGroup(userIdList.stream().collect(Collectors.joining(",")) , ctx.executor());
        //2. 筛选出待加入群聊的channel和userName

        for (String userId:userIdList
             ) {
            Channel channel = SessionUtil.getChannel(userId);
            if (channel != null){
                channelGroup.add(channel);
                String userName = SessionUtil.getSession(channel).getUserName();
                userNameList.add(userName);
            }
        }


        CreateGroupResponsePacket createGroupResponsePacket = new CreateGroupResponsePacket();
        createGroupResponsePacket.setGroupId(IDUtil.randomId());
        createGroupResponsePacket.setSuccess(true);
        createGroupResponsePacket.setUserNameList(userNameList);

        channelGroup.writeAndFlush(createGroupResponsePacket);

        logger.info("群创建成功，id 为[{}] ", createGroupResponsePacket.getGroupId());
        logger.info("群里面有：{}" , createGroupResponsePacket.getUserNameList() );
    }

}
