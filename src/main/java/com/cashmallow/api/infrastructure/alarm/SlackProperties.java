package com.cashmallow.api.infrastructure.alarm;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;

@ConstructorBinding
@ConfigurationProperties(prefix = "slack")
public record SlackProperties(
        String apiUrl,
        String token,
        String tokenJp,
        String senderName,
        Map<SlackChannel, String> channelId,
        Webhook webhook
) {
    public ChannelIdAndToken getChannelIdAndToken(SlackChannel channel,
                                                  CountryCode sendCountry) {
        final boolean isJp = sendCountry == CountryCode.JP;
        String cId = switch (channel) {
            case ADMIN_ALERT -> channelId.get(isJp ? SlackChannel.ADMIN_ALERT_JP : SlackChannel.ADMIN_ALERT);
            case ADMIN_MESSAGE -> channelId.get(isJp ? SlackChannel.ADMIN_MESSAGE_JP : SlackChannel.ADMIN_MESSAGE);
            case ADMIN_EDD -> channelId.get(isJp ? SlackChannel.ADMIN_EDD_JP : SlackChannel.ADMIN_EDD);
            default -> channelId.get(channel);
        };
        String tKen = switch (channel) {
            case ADMIN_ALERT, ADMIN_MESSAGE, ADMIN_EDD -> isJp ? tokenJp : token;
            default -> token;
        };

        return new ChannelIdAndToken(cId, tKen);
    }

    public record Webhook(
            String token
    ) {
    }

    public record ChannelIdAndToken(
            String channelId,
            String token
    ) {
    }

}
