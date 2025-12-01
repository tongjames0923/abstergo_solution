package com.tbs.rabbitmq.config;

import com.tbs.common.AbstractAutoBeanRegister;

public class RabbitMqAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "RabbitMQ Pack";
    }

    @Override
    protected String[] getBasePackages() {
        return new String[] {"com.tbs.rabbitmq"};
    }
}
