package io.github.meridian.mixin;

import io.github.meridian.utils.TickScheduler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Hypixel sends a ClientboundPingPacket with a non-zero id once per server
// tick as part of their latency-tracking scheme. Intercepting it at the
// Netty layer gives us a true 20 TPS server-paced tick stream, so scheduled
// tasks (countdowns, etc.) stay aligned with the server through lag.
@Mixin(Connection.class)
public class ConnectionMixin {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void meridian$onServerTick(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundPingPacket ping && ping.getId() != 0) {
            TickScheduler.onServerTick();
        }
    }
}