package com.cloudm.sign.token;

import com.cloudm.framework.common.enums.BaseErrorEnum;
import com.cloudm.framework.common.ex.BusinessCheckFailException;
import com.cloudm.sign.dao.ApplicationSessionContext;
import com.cloudm.sign.enums.TokenErrorEnum;
import com.google.gson.Gson;
import com.cloudm.sign.context.DefaultContextHolder;
import com.cloudm.sign.constant.Constants;
import com.cloudm.sign.utils.EncryptUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.TimeUnit;

/**
 * @author Jay
 * @version v1.0
 * @description token相关操作
 * @date 2018-03-02 15:10
 */

public class DefaultTokenHelper<S extends ApplicationSessionContext> {


    public RedisTemplate<String, String> redisTemplate ;


    public DefaultContextHolder<S> defaultContextHolder;


    public Class<S> entityClass;


    public DefaultTokenHelper(RedisTemplate redisTemplate, DefaultContextHolder defaultContextHolder){
        this.redisTemplate = redisTemplate;
        this.defaultContextHolder = defaultContextHolder;
        this.entityClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 校验token是否有效
     *
     * @param token
     */
    public Boolean verifyToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "token不能为空");
        }
        // 根据token生成存入redis的key,对应的value为用户信息
        String key = this.generateRedisUserInfoKey(token);
        // 获取用户信息
        String str = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(str)) {
            throw new BusinessCheckFailException(TokenErrorEnum.TOKEN_OVER_DUE_ERROR.getKey(), TokenErrorEnum.TOKEN_OVER_DUE_ERROR.getName());
        }
        // 解析取出来的是业务session对象
        S s = (new Gson()).fromJson(str,entityClass);
        // 初始化本地业务session
        defaultContextHolder.initSessionContext(s.getAccountName());
        // 重置时间
        redisTemplate.expire(key, Constants.TWO_DAYS, TimeUnit.DAYS);
        // 根据token生成存入redis的key,对应的value为用户信息
        String userInfoKey = this.generateRedisTokenKey(s.getId());
        // 重置时间
        redisTemplate.expire(userInfoKey, Constants.TWO_DAYS, TimeUnit.DAYS);

        return Boolean.TRUE;
    }


    /**
     * 根据token获取存储在redis的用户信息
     *
     * @param token
     * @return
     */
    public S getUserInfoByToken(String token){
        if (StringUtils.isEmpty(token)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "token不能为空");
        }
        // 根据token生成存入redis的key,对应的value为用户信息
        String key = this.generateRedisUserInfoKey(token);
        // 获取用户信息
        String str = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return (new Gson()).fromJson(str,entityClass);
    }


    /**
     * 根据userId获取存储在redis的用户信息
     *
     * @param userId
     * @return
     */
    public S getUserInfoByUserId(Integer userId){
        if (userId == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "userId不能为空");
        }
        String key = this.generateRedisTokenKey(userId);
        String token = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return getUserInfoByToken(token);
    }


    /**
     * 保存用户信息至
     * redis key："alliance:str:user_key:"加上 token字符串
     *
     * @param sessionContext 这是为了扩展存入redis的业务session对象，只要继承{@link ApplicationSessionContext}后重新加入需要的字段即可
     */
    public String saveUserToRedis(S sessionContext) {
        if (sessionContext == null || sessionContext.getId() == null) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "account和userId都不能为空");
        }
        // 生成token
        String token = this.generateToken(sessionContext.getId());
        // 保存 key ：token , value : 用户信息
        String key = this.generateRedisUserInfoKey(token);
        redisTemplate.opsForValue().set(key, new Gson().toJson(sessionContext), Constants.TWO_DAYS, TimeUnit.DAYS);
        // 保存 key : id ，value : token
        String tokenKey = generateRedisTokenKey(sessionContext.getId());
        redisTemplate.opsForValue().set(tokenKey, token, Constants.TWO_DAYS, TimeUnit.DAYS);
        // 返回token
        return token;
    }

    /**
     * 根据id清空redis用户的登录信息
     *
     * @param userId
     */
    public void cleanRedisSessionById(Integer userId){
        if(userId == null){
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),"userId不能为空");
        }
        // 清空redis的登录id对应的token信息
        String key = this.generateRedisTokenKey(userId);
        String token = redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(token)){
            redisTemplate.delete(key);
        } else {
            return;
        }
        // 清空redis的登录的token对应的用户信息
        String userInfoKey = this.generateRedisUserInfoKey(token);
        String json = redisTemplate.opsForValue().get(userInfoKey);
        if(!StringUtils.isEmpty(json)){
            redisTemplate.delete(userInfoKey);
        }
    }


    /**
     * 根据用户id判断该账号是否已经登录
     *
     * @param userId
     * @return true: 登录, false: 未登录
     */
    public Boolean isLogin(Integer userId){
        if(userId == null){
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),"userId不能为空");
        }
        String key = this.generateRedisTokenKey(userId);
        String token = redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(token)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 拼接验证码的redis的key
     *
     * @param key
     * @param salt
     * @return
     */
    public String getRedisKey(String key, String salt) {
        if (StringUtils.isEmpty(key)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "key不能为空");
        }
        return new StringBuilder(salt).append(key).toString();
    }


    /*=============================================private method==================================================*/


    /**
     * 根据token生成存入redis的key,对应的value为用户信息
     *
     * @param token
     * @return
     */
    private String generateRedisUserInfoKey(String token) {
        return this.getRedisKey(token, this.getBaseKey());
    }

    /**
     * 根据token生成存入redis的key,对应的value为用户信息
     *
     * @param userId
     * @return
     */
    private String generateRedisTokenKey(Integer userId) {
        return this.getRedisKey(userId.toString(), this.getRedisTokenBaseKey());
    }


    /**
     * 基础的redis的key
     *
     * @return
     */
    private String getBaseKey(){
        String path = this.getRedisSavePath();
        if(!StringUtils.isEmpty(path)){
            return path;
        }
        return Constants.USER_KEY;
    }

    /**
     * 基础的token的key
     *
     * @return
     */
    private String getRedisTokenBaseKey(){
        String path = this.getRedisTokenPath();
        if(!StringUtils.isEmpty(path)){
            return path;
        }
        return Constants.TOKEN_KEY;
    }

    /**
     * 生成token
     *
     * @param userId 用户Id
     * @return
     */
    private  String generateToken(Integer userId){
        if(userId == null){
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(),"userId不能为空");
        }
        String str = new StringBuilder(userId.toString()).append(System.currentTimeMillis()).toString();
        return EncryptUtil.getToken(str);
    }

    /**
     * id存入redis的路径，如果子类不重写该方法则选用默认路径 {@link Constants}的USER_KEY
     *
     * @return 建议格式为 "项目名称:str:user_key:"
     */
    protected String getRedisSavePath(){
        return null;
    }


    /**
     * token存入redis的路径，如果子类不重写该方法则选用默认路径 {@link Constants}的TOKEN_KEY
     *
     * @return 建议格式为 "项目名称:str:token_key:"
     */
    protected String getRedisTokenPath(){
        return null;
    }

}
