package com.allen.canel.client.base;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author snh
 * @description: TODO
 * @date 2022/4/11
 */
public class BaseCanalClient {

    protected static final Logger log = LoggerFactory.getLogger(BaseCanalClient.class);

    protected static final String SEP = SystemUtils.LINE_SEPARATOR;

    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected volatile boolean running = false;

    protected Thread.UncaughtExceptionHandler handler = (t, e) -> log.error("parse events has an error", e);

    protected Thread thread = null;

    protected CanalConnector connector;

    protected static String context_format = null;

    protected static String row_format = null;

    protected static String transaction_format = null;

    protected String destination;

    static {
        context_format = SEP + "****************************************************" + SEP;
        context_format += "* Batch Id: [{}] ,count : [{}] , memsize : [{}] , Time : {}" + SEP;
        context_format += "* Start : [{}] " + SEP;
        context_format += "* End : [{}] " + SEP;
        context_format += "****************************************************" + SEP;

        row_format = SEP
                + "----------------> binlog[{}:{}] , name[{},{}] , eventType : {} , executeTime : {}({}) , gtid : ({}) , delay : {} ms"
                + SEP;

        transaction_format = SEP
                + "================> binlog[{}:{}] , executeTime : {}({}) , gtid : ({}) , delay : {}ms"
                + SEP;
    }


    protected void printSummary(Message message, long batchId, int size) {
        long memsize = 0;
        for (CanalEntry.Entry entry : message.getEntries()) {
            memsize += entry.getHeader().getEventLength();
        }
        String startPosition = null;
        String endPosition = null;
        if (!CollectionUtils.isEmpty(message.getEntries())) {
            startPosition = buildPositionForDump(message.getEntries().get(0));
            endPosition = buildPositionForDump(message.getEntries().get(message.getEntries().size() - 1));
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        log.info(context_format, new Object[]{batchId, size, memsize, sdf.format(new Date()), startPosition, endPosition});
    }

    protected String buildPositionForDump(CanalEntry.Entry entry) {
        long time = entry.getHeader().getExecuteTime();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String position = entry.getHeader().getLogfileName() + ":" + entry.getHeader().getLogfileOffset() + ":" +
                entry.getHeader().getExecuteTime() + "(" + sdf.format(date) + ")";
        if (StringUtils.isNotEmpty(entry.getHeader().getGtid()))
            position += " gtid(" + entry.getHeader().getGtid() + ")";
        return position;
    }

    protected void printEntry(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            long executeTime = entry.getHeader().getExecuteTime();
            long delayTime = new Date().getTime() - executeTime;
            Date date = new Date(executeTime);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                //类型为事务开始
                if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN) {
                    CanalEntry.TransactionBegin begin = null;
                    try {
                        begin = CanalEntry.TransactionBegin.parseFrom(entry.getStoreValue());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException("Parse event has an error, data：" + entry.toString(), e);
                    }
                    //打印事务头信息，执行的线程id。事务耗时
                    log.info(transaction_format, new Object[]{entry.getHeader().getLogfileName(),
                            String.valueOf(entry.getHeader().getLogfileOffset()),
                            String.valueOf(entry.getHeader().getExecuteTime()),
                            sdf.format(date),
                            entry.getHeader().getGtid(),
                            String.valueOf(delayTime)
                    });
                    log.info("BEGIN ----> Thread id：{}",  begin.getThreadId());
                    printXAInfo(begin.getPropsList());
                } else if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                    CanalEntry.TransactionEnd end = null;
                    try {
                        end = CanalEntry.TransactionEnd.parseFrom(entry.getStoreValue());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException("Parse event has an error, data：" + entry.toString(), e);
                    }
                    //打印事务提交信息
                    log.info("-----------------\n");
                    log.info("END ----> transaction id：{}", end.getTransactionId());
                    printXAInfo(end.getPropsList());
                    log.info(transaction_format, new Object[]{
                            entry.getHeader().getLogfileName(),
                            String.valueOf(entry.getHeader().getLogfileOffset()),
                            String.valueOf(entry.getHeader().getExecuteTime()),
                            sdf.format(date),
                            entry.getHeader().getGtid(),
                            String.valueOf(delayTime)
                    });
                }
                continue;
            }

            log.info("Operation schema name：{}, table name：{}",entry.getHeader().getSchemaName(), entry.getHeader().getTableName());
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                CanalEntry.RowChange rowChange = null;
                try {
                    rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException("Parse event has an error, data：" + entry.toString(), e);
                }
                CanalEntry.EventType eventType = rowChange.getEventType();
                log.info(row_format, new Object[]{
                        entry.getHeader().getLogfileName(),
                        String.valueOf(entry.getHeader().getLogfileOffset()),
                        entry.getHeader().getSchemaName(),
                        entry.getHeader().getTableName(),
                        eventType,
                        String.valueOf(entry.getHeader().getExecuteTime()),
                        sdf.format(date),
                        entry.getHeader().getGtid(),
                        String.valueOf(delayTime)
                });

                if (eventType == CanalEntry.EventType.QUERY || rowChange.getIsDdl()) {
                    log.info("ddl：" + rowChange.getIsDdl() + ", Sql------------>" + rowChange.getSql() + SEP);
                    continue;
                }
                printXAInfo(rowChange.getPropsList());
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    if (eventType == CanalEntry.EventType.DELETE) {
                        printColumn(rowData.getBeforeColumnsList());
                    } else if (eventType == CanalEntry.EventType.INSERT) {
                        printColumn(rowData.getAfterColumnsList());
                    } else {
                        printColumn(rowData.getAfterColumnsList());
                    }
                }
            }
        }
    }

    protected void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            StringBuilder sb = new StringBuilder();
            try {
                if (StringUtils.containsIgnoreCase(column.getMysqlType(), "BLOB")
                        || StringUtils.containsIgnoreCase(column.getMysqlType(), "BINARY")) {
                    //get value bytes
                    sb.append(column.getName() + " : " + new String(column.getValue().getBytes("ISO-8859-1"), "UTF-8"));
                } else {
                    sb.append(column.getName() + " : " + column.getValue());
                }
            } catch (UnsupportedEncodingException e) {
            }
            sb.append("     type=" + column.getMysqlType());
            if (column.getUpdated()) {
                sb.append("     update=" + column.getUpdated());
            }
            sb.append(SEP);
            log.info(sb.toString());
        }
    }

    protected void printXAInfo(List<CanalEntry.Pair> pairs) {
        if (pairs == null) {
            return;
        }

        String xaType = null;

        String xaXid = null;

        for (CanalEntry.Pair pair : pairs) {
            String key = pair.getKey();
            if (StringUtils.endsWithIgnoreCase(key, "XA_INFO")) {
                xaType = pair.getValue();
            } else if (StringUtils.endsWithIgnoreCase(key, "XA_XID")) {
                xaXid = pair.getValue();
            }
        }
        if (xaType != null && xaXid != null) {
            log.info(" ------> " + xaType + " " + xaXid);
        }
    }

    public void setConnector(CanalConnector connector) {
        this.connector = connector;
    }

    /**
     * 获取当前Entry的 GTID信息示例
     *
     * @param header
     * @return
     */
    public static String getCurrentGTid(CanalEntry.Header header) {
        List<CanalEntry.Pair> props = header.getPropsList();
        if (props != null && props.size() > 0) {
            for (CanalEntry.Pair prop : props) {
                if ("curtGtid".equals(prop.getKey()))
                    return prop.getValue();
            }
        }
        return "";
    }

    /**
     * 获取当前Entry的 GTID Sequence No信息示例
     *
     * @param header
     * @return
     */
    public static String getCurrentGtidSn(CanalEntry.Header header) {
        List<CanalEntry.Pair> props = header.getPropsList();
        if (props != null && props.size() > 0) {
            for (CanalEntry.Pair prop : props) {
                if ("curtGtidSn".equals(prop.getKey()))
                    return prop.getValue();
            }
        }
        return "";
    }

    /**
     * 获取当前Entry的 GTID Last Committed信息示例
     *
     * @param header
     * @return
     */
    public static String getCurrentGtidLct(CanalEntry.Header header) {
        List<CanalEntry.Pair> props = header.getPropsList();
        if (props != null && props.size() > 0) {
            for (CanalEntry.Pair pair : props) {
                if ("curtGtidLct".equals(pair.getKey())) {
                    return pair.getValue();
                }
            }
        }
        return "";
    }

}
