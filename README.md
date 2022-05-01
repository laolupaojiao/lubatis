# spring-thinkphp
将thinkphp对mysql的操作方式移植到spring


一、简单使用-查询:

new BaseDao().select("user").alins("u").field("*").where(new String[][]{{"nickname", "=", "老路"},{"username", "=", "123456"}}).join(new String[]{"plant p","p.p_id = u.id","left"}).execQuery()

解析: 
1、select - 传入string类型字符串，代表表名 user
2、alins - 传入string类型字符串 ，将user表取别名 u
3、where - 传入二维数组，单个元素格式为 字段名,条件,值 例如  {"nickname", "=", "老路"}
4、join - 传入一维数组，格式为 表名 别名,条件 如{"plant p","p.p_id = u.id","left"}

如不设置field，默认字段为 *
默认execQuery()方法返回类型为List<Map>，方法可传入实体类进行接收
如，我想用User类来接收返回值，则execQuery(User.class)

二、简单使用-更新:

new BaseDao().update("user").set(new String[][]{{"username","12345"}}).where(new String[][]{{"username", "=", "1234"}}).execUpdate()

解析: 
1、update - 传入string类型字符串，代表表名 user
2、set - 传入二维数组，单个元素格式为 字段名,值 例如  {"username","12345"}
3、where - 传入二维数组，单个元素格式为 字段名,条件,值 例如  {"nickname", "=", "老路"}
  
三、实体类继承BaseDao
  
  实体类可设置table属性，对应表名，不设置则默认类名的全小写为表名 例如： private String table = "user";
  实体类可设置pk属性，对应主键名，不设置默认为id  private String pk = "id";

  例如当前有一个User extends BaseDao
  
  则插入语句可以更改为
  
  User user = new User();
  user.setPassword("admin");
  user.setUsername("admin");
  user.setNickname("管理员");
  user.setEmail("laolupaojiao@foxmail.com");
  执行user.save();即可插入
  
  同理，查询语句可以更改为
  User user = new User();
  执行user.find("1"); 默认返回List<T> , 须get(0)
  注意：find方法默认用主键查询，主键默认为id
  
  
  三、多条语句事务
  
  数据库：id=>7	username=>1234
  例如：
  List<BaseDao> ql = new ArrayList<>();
  ql.add(new BaseDao().update("user").set(new String[][]{{"username","12345"}}).where(new String[][]{{"username", "=", "1234"}}));
  ql.add(new BaseDao().update("user").set(new String[][]{{"username","123456"}}).where(new String[][]{{"username", "=", "12346"}}));
  return JsonResponse.Success(new BaseDao().transaction(ql));
  
  调用transaction方法，传入BaseDao类型的List，执行多条语句，案例因找不到12346导致返回结果为false，原数据不会改变
  
  
