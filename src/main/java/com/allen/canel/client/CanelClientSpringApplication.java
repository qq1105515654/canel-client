package com.allen.canel.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.allen.canel.client.annotaion.CanalComponentScan;
import com.allen.canel.client.base.AbstractCanalClientTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.net.InetSocketAddress;

/**
 * @author snh
 * @description: TODO
 * @date 2022/4/9
 */
@CanalComponentScan(packages = {"com.allen.canel.client.model"})
@SpringBootApplication
public class CanelClientSpringApplication {


    public static void main(String[] args) {
        SpringApplication.run(CanelClientSpringApplication.class, args);
    }

    @Component
    final class RunnerListener implements ApplicationRunner {
        private final Logger log= LoggerFactory.getLogger(RunnerListener.class);

        @Override
        public void run(ApplicationArguments args) throws Exception {

            Signal sg=new Signal("TERM");//kill -15 pid
            Signal.handle(sg, new SignalHandler() {
                @Override
                public void handle(Signal signal) {
                    System.exit(0);
                }
            });
            SimpleCanalClient canalClient = new SimpleCanalClient("example");
            canalClient.startListener("120.79.156.76",11111);

            Runtime.getRuntime().addShutdownHook(new Thread(()->{

                try {
                    log.info("## Stop the canal client");
                    canalClient.stop();
                } catch (Throwable e) {
                    log.warn("### Something goes wrong when stopping canal：",e);
                }finally {
                    log.info("## canal client is down.");
                }
            }));
        }
    }

    final class SimpleCanalClient extends AbstractCanalClientTest{

        public SimpleCanalClient(String destination) {
            super(destination);
        }

        public void startListener(String host,int port){
            CanalConnector example = CanalConnectors.newSingleConnector(new InetSocketAddress(host, port), destination, "admin", "");
            this.setConnector(example);
            this.start();
        }
    }

}
