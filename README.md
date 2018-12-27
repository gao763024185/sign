

# 云机械登录模块快速集成框架



## 项目更新日志

#### 2018-03-14 v1.0.0 集成新功能
* 项目开发、测试完成。



## 项目主要功能

* 快速进行登录，密码校验。
* 支持注解关闭登录认证。
* 在需要的时候能非常简单的注入业务session。

## 框架集成


* 将需要保存的用户业务session的类 `SessionContext` 继承 `ApplicationSessionContext` 类。需要注意的是id和accountName已经在父类内了，不要在子类重新去声明一样的成员变量。
```java
@Data
public class SessionContext extends ApplicationSessionContext{
    
    // TODO 添加需要的存入用户业务session的成员变量

    public SessionContext(Integer id, String accountName) {
        super(id, accountName);
    }
}
```
* 新建一个 `ContextHolder` 类继承 `DefaultContextHolder` 类。注意这里的 `ContextHolder` 的bean的id必须为contextHolder，因为在后面的拦截器的bean在spring中注入了bean的id为contextHolder的类。下面的 `TokenHelper` 类也是如此。在这里还需要注意的一点就是 `setSessionContext(String userName)` 这个方法在为SessionContext对象填充参数的时候，要注意这里的id即为当前登录用户的id，如果不一致，那么在redis中将会出现该id对应的token无法找到用户业务session信息。因为redis中存储的格式为id对应token，token对应用户业务session信息。

```java
@Component
public class ContextHolder extends DefaultContextHolder<SessionContext>{

    /**
     * 设置业务session, 并且该业务session对象将会存入redis中, 子类必须实现
     *
     * @param userName
     * @return
     */
    @Override
    public SessionContext setSessionContext(String userName) {
        // 将需要保存的业务session进行初始化 TODO
        return null;
    }
}
```
* 新建一个 `TokenHelper` 类继承 `DefaultTokenHelper` 类。并在其中通过构造器注入 `redisTemplate` 和 `contextHolder`。

```java
@Component
public class TokenHelper extends DefaultTokenHelper<SessionContext>{


    /**
     * 注入需要的对象
     *
     * @param redisTemplate
     * @param contextHolder
     */
    @Autowired
    public TokenHelper(RedisTemplate redisTemplate, ContextHolder contextHolder) {
        super(redisTemplate, contextHolder);
    }
}
```

* 新建一个 `LoginService` 类继承 `DefaultSignFactoryBean` 类。登录的时候调用 `login(String userName, String password)` 方法即可，其中 `this.doLogin(userName, password, Boolean.TRUE)` 方法的第三个参数为是否允许同时多处登陆。
```java
@Component
public class LoginService extends DefaultSignFactoryBean<SessionContext> {


    /**
     * 注入实例至父类构造器
     *
     * @param contextHolder
     * @param tokenHelper
     */
    @Autowired
    public LoginManager(ContextHolder contextHolder, TokenHelper tokenHelper) {
        super(contextHolder, tokenHelper);
    }


    /**
     * 登陆
     *
     * @param userName
     * @param password
     * @return
     */
    public SignResult login(String userName, String password) {
        return this.doLogin(userName, password, Boolean.FALSE);
    }

    /**
     * 根据userName获取userId
     *
     * @param userName
     * @return userId
     */
    @Override
    public Integer getUserIdByUserName(String userName) {
        // 返回当前登录的userId TODO
        return null;
    }

    /**
     * 密码校验
     *
     * @param userName
     * @param password
     * @return 当密码校验正确的还是返回null, 否则返回错误提示信息
     */
    @Override
    public String judgeAccount(String userName, String password) {
        // 进行密码校验 TODO
        return null;
    }
}
```
* 需要前端配合在每个请求头中加入token，字段名称为token。

* 至此，该框架集成完毕。



## 如何使用

* 获取当前登录用户的业务session信息
```java
@Component
public class Example{

    @Autowired
    private ContextHolder contextHolder;
    
    SessionContext sessionContext = contextHolder.getSessionContext();
}
```
* 某些接口不需要进行token认证，可以使用下面的 `@NotCertification` 注解。
```java
@Controller
public class Example{

    @NotCertification
    @RequestMapping("/login")
    @ResponseBody
    public Result<?> login() {
    return Result.wrapSuccessfulResult("不需要进行token认证,可直接访问");
    }
}
```
* 调用登录的方法。根据返回对象判断是否登录成功，如果成功会返回token。
```java
@Component
public class Example{

    @Autowired
    private LoginManager loginManager;

    SignResult signResult = loginManager.login("userName","password");
}
```

## 写在最后

* 后续有出现问题或者需要扩展功能可以联系相关开发人员。



  