package com.allen.canel.client.base;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import org.slf4j.MDC;
import org.springframework.util.Assert;

/**
 * @author snh
 * @description: TODO 测试基类
 * @date 2022/4/9
 */
public abstract class AbstractCanalClientTest extends BaseCanalClient {

    public AbstractCanalClientTest(String destination) {
        this(destination, null);
    }

    public AbstractCanalClientTest(String destination, CanalConnector connector) {
        this.destination = destination;
        this.connector = connector;
    }

    protected void start() {
        Assert.notNull(connector, "connector is null");
        thread = new Thread(this::process);
        thread.setUncaughtExceptionHandler(handler);
        running = true;
        thread.start();
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
//                e.printStackTrace();
                //ignore
            }
        }
        MDC.remove("destination");
    }

    private void process() {
        int batchSize = 5 * 1024;
        while (running) {
            MDC.put("destination", destination);
            connector.connect();
            connector.subscribe("");//订阅连接
            try {
                while (running) {
                    Message message = connector.getWithoutAck(batchSize);//获取指定数量的数据
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        //ignore
                    } else {
                        printSummary(message, batchId, size);
                        printEntry(message.getEntries());
                    }
                    if (batchId != -1) {
                        connector.ack(batchId);//确认提交
                    }
                }
            }finally {
                connector.disconnect();
                MDC.remove("destination");
            }
        }
    }

}
