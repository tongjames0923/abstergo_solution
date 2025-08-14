package com.tbs.transaction.scalable.info;

import java.io.*;

import com.tbs.common.data.ValueWrapper;
import com.tbs.common.function.SConsumer;

/**
 * 默认可回滚数据实现类
 * <p>
 * 该类实现了{@link IRollbackableData}接口，提供了数据的提交、回滚和获取功能。
 * 在提交数据时会保存当前数据的深拷贝作为备份，回滚时恢复到之前的状态。
 * </p>
 */
public class DefaultRollbackableData implements IRollbackableData {
	private static final long serialVersionUID = 1L;

	/**
	 * 当前数据值
	 */
	private Serializable currentValue;

	/**
	 * 备份数据值，用于回滚操作
	 */
	private Serializable backupValue;

	/**
	 * 构造函数
	 *
	 * @param initialValue 初始值
	 */
	public DefaultRollbackableData(Serializable initialValue) {
		setValue(initialValue);
	}

	/**
	 * 默认构造函数
	 */
	public DefaultRollbackableData() {
	}

	/**
	 * 回滚数据
	 * <p>
	 * 该方法用于撤销对数据的更改，将数据恢复到上次提交时的状态。
	 * </p>
	 *
	 * @see IRollbackableData#rollback()
	 */
	@Override
	public void rollback() {
		// 使用深拷贝恢复备份值
		this.currentValue = deepCopy(this.backupValue);
	}

	/**
	 * 提交数据
	 * <p>
	 * 该方法用于确认对数据的更改，使数据更改永久生效。 在提交时会将当前值进行深拷贝并备份，以便在需要时可以回滚。
	 * </p>
	 *
	 * @param dataSetter 数据设置器
	 * @see IRollbackableData#commit(SConsumer)
	 */
	@Override
	public void commit(SConsumer<ValueWrapper<Serializable>> dataSetter, boolean copyFlag) {
		// 备份当前值的深拷贝
		this.backupValue = copyFlag ? deepCopy(this.currentValue) : this.currentValue;

		// 创建值包装器并应用数据设置器
		ValueWrapper<Serializable> wrapper = ValueWrapper.of(this.currentValue);
		dataSetter.accept(wrapper);

		// 更新当前值
		this.currentValue = wrapper.get().orElse(null);
	}

	/**
	 * 获取数据
	 * <p>
	 * 该方法用于获取当前数据对象的值。
	 * </p>
	 *
	 * @return Serializable 当前数据对象的值
	 * @see IRollbackableData#getData()
	 */
	@Override
	public Serializable getData() {
		return this.currentValue;
	}

	/**
	 * 设置值并创建初始备份
	 *
	 * @param value 要设置的值
	 */
	private void setValue(Serializable value) {
		this.currentValue = value;
		this.backupValue = deepCopy(value);
	}

	/**
	 * 深拷贝一个可序列化对象
	 *
	 * @param original 原始对象
	 * @return 深拷贝后的对象
	 */
	@SuppressWarnings("unchecked")
	private <T extends Serializable> T deepCopy(T original) {
		if (original == null) {
			return null;
		}

		try {
			// 使用序列化进行深拷贝
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(original);
			oos.flush();

			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bis);
			return (T) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// 如果序列化失败，尝试其他方式
			throw new RuntimeException("深拷贝失败: " + e.getMessage(), e);
		}
	}
}
