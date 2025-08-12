package com.tbs.transaction.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.StrUtil;
import com.tbs.lock.util.LockUtil;
import com.tbs.transaction.ITransaction;
import com.tbs.transaction.ITransactionInfo;
import com.tbs.transaction.TransactionStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 基于本地文件系统的事务处理
 *
 * @author abstergo
 */
@Slf4j
public abstract class AbstractLocalFileSystemTransaction implements ITransaction {
    private String dirPath;
    private File dir;
    private String suffix = ".lock";

    public AbstractLocalFileSystemTransaction(String dirPath, String suffix) {
        this.dirPath = dirPath;
        this.dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("创建事物目录：{}", dirPath);
        }
        AssertUtil.isTrue(!dir.isDirectory(), "{} 不是一个目录", dirPath);
        if (StrUtil.isEmpty(suffix)) {
            this.suffix = ".lock";
        }
        this.suffix = suffix;

    }

    public File getDirPath() {
        return dir;
    }

    private File makeTransactionFile(String transactionId) {
        File file = new File(dir, transactionId + suffix);
        return file;
    }

    protected byte[] serialize(ITransactionInfo transactionInfo) {
        return JSON.toJSONString(transactionInfo).getBytes(StandardCharsets.UTF_8);
    }

    protected ITransactionInfo deserialize(byte[] transactionInfo) {
        return JSON.parseObject(new String(transactionInfo, StandardCharsets.UTF_8), DefaultTransactionInfo.class);
    }

    @Override
    public ITransactionInfo beginTransaction(String transactionId) {
        ValueWrapper<ITransactionInfo> transactionInfo = new ValueWrapper<>();
        LockUtil.getInstance().lockThen(transactionId, () -> {
            File file = makeTransactionFile(transactionId);
            AssertUtil.isTrue(file.exists(), "{} 事务已经存在。请等待结束后再开启新的事务", transactionId);
            try {
                boolean created = file.createNewFile();
                AssertUtil.isTrue(created, "{} 创建事务文件失败", transactionId);
            } catch (Throwable e) {
                AssertUtil.throwError(() -> {
                    return new FormattedRuntimeException(e, "{} 事务创建失败", transactionId);
                });
            }
            DefaultTransactionInfo info = new DefaultTransactionInfo(transactionId, this);
            info.setTransactionStatus(TransactionStatus.BEGIN);
            write(transactionId, file, info, transactionInfo);
        });

        return transactionInfo.get().orElseThrow(() -> {
            return new FormattedRuntimeException("{} 获取事务信息失败", transactionId);
        });
    }

    private void write(String transactionId, File file, ITransactionInfo info,
        ValueWrapper<ITransactionInfo> transactionInfo) {
        try {
            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
            writer.write(serialize(info));
            writer.flush();
            writer.close();
            transactionInfo.set(info);
        } catch (IOException e) {
            AssertUtil.throwError(() -> {
                return new FormattedRuntimeException(e, "事务{}数据写入失败", transactionId);
            });
        }
    }

    @Override
    public void commit(ITransactionInfo transactionInfo) {
        ValueWrapper<ITransactionInfo> info = new ValueWrapper<>();
        LockUtil.getInstance().lockThen(transactionInfo.getTransactionId(), () -> {
            String key = transactionInfo.getTransactionId();
            File file = makeTransactionFile(key);
            AssertUtil.isTrue(!file.exists(), "{} 事务文件不存在", key);
            transactionInfo.setTransactionStatus(TransactionStatus.COMMIT);
            write(key, file, transactionInfo, info);
        });
    }

    @Override
    public void rollback(ITransactionInfo transactionInfo) {
        ValueWrapper<ITransactionInfo> info = new ValueWrapper<>();
        LockUtil.getInstance().lockThen(transactionInfo.getTransactionId(), () -> {
            String key = transactionInfo.getTransactionId();
            File file = makeTransactionFile(key);
            AssertUtil.isTrue(!file.exists(), "{} 事务文件不存在", key);
            transactionInfo.setTransactionStatus(TransactionStatus.ROLLBACK);
            write(key, file, transactionInfo, info);
        });
    }

    @Override
    public void complete(ITransactionInfo transactionInfo) {
        String key = transactionInfo.getTransactionId();
        LockUtil.getInstance().lockThen(key, () -> {
            File file = makeTransactionFile(key);
            AssertUtil.isTrue(!file.exists(), "{} 事务文件不存在", key);
            file.delete();
        });
    }

    @Override
    public Optional<ITransactionInfo> getTransaction(String key) {
        ValueWrapper<ITransactionInfo> transactionInfo = new ValueWrapper<>();
        LockUtil.getInstance().lockThen(key, () -> {
            File file = makeTransactionFile(key);
            AssertUtil.isTrue(!file.exists(), "{} 事务文件不存在", key);
            BufferedInputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                byte[] bytes = inputStream.readAllBytes();
                inputStream.close();
                ITransactionInfo info = deserialize(bytes);
                transactionInfo.set(info);
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                AssertUtil.throwError(() -> new FormattedRuntimeException(e, "事务{}数据读取失败", key));
            }
        });
        return transactionInfo.get();
    }
}
