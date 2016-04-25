# modules-sequence 用法

### 1、 表的脚本###
    DROP TABLE IF EXISTS `sequence`;
    CREATE TABLE `sequence` (
      `value` bigint(20) DEFAULT NULL,
      `step` int(11) DEFAULT NULL,
      `retryTimes` int(11) DEFAULT NULL,
      `gmt_modified` datetime DEFAULT NULL,
      `name` varchar(255) DEFAULT NULL
     ) ENGINE=InnoDB DEFAULT CHARSET=utf-8;
### 2、 引入包###
      <dependency>
			<groupId>com.sunney</groupId>
			<artifactId>modules-common</artifactId>
			<version>1.0-SNAPSHOT</version>
	  </dependency>
### 3、 新建配置文件 spring-sequence.xml###
      <?xml version="1.0" encoding="gb2312"?>
      <!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
     <beans>
	 
	   <bean id="sequenceDao" class="com.sunney.modules.sequence.impl.DefaultSequenceDao"
			lazy-init="default" autowire="default" dependency-check="default">
			<property name="dataSource">
				<ref bean="DBCOMM" /> --数据源，必须正确
			</property>
			<!-- 以下是这个DAO所有的可选配置，其value是默认的配置 一般来说不用去动他们，和DBA商量好使用这些默认的值就行了 retryTimes表示重试次数，因为是乐观锁去取，所以会有最大重试次数，如果超过这个次数会抛出一个异常。 
				step 表示一次更新数据库的值是多少，默认是1000 tableName 表示这个表在数据库中的名字 nameColumnName 表示这个sequence名字那列在数据库中的列名 
				nameColumnName 表示这个sequence的值那列在数据库中的列名 gmtModifiedColumnName 表示最后修改时间那个列的在数据库中的列名 -->
			<property name="retryTimes">
				<value>3</value>
			</property>
			<property name="step">
				<value>1000</value>
			</property>
			<property name="tableName">
				<value>sequence</value>
			</property>
			<property name="nameColumnName">
				<value>name</value>
			</property>
			<property name="valueColumnName">
				<value>value</value>
			</property>
			<property name="gmtModifiedColumnName">
				<value>gmt_modified</value>
			</property>
	  </bean>
		<bean id="sequence" class="com.sunney.modules.sequence.impl.DefaultSequence"
			lazy-init="default" autowire="default" dependency-check="default">
			<property name="sequenceDao">
				<ref bean="sequenceDao" />
			</property>	
		</bean>
    </beans>
必须要import文件 spring-sequence.xml

### 4、 应用###
       @Autowired
       private Sequence sequence;
 
       sequence.nextValue() 返回默认的SequenceName的序号
       或sequence.nextValue(String SequenceName) 返回指定的SequenceName的序号

对应的SequenceName必须初始化的插入记录进去，指定序号初始化大小！
 

     