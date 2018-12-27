package com.cloudm.sign.interceptor;

import com.cloudm.framework.common.enums.BaseErrorEnum;
import com.cloudm.framework.common.ex.BusinessCheckFailException;
import com.cloudm.framework.common.web.result.Result;
import com.cloudm.sign.annotation.SecurityValidate;
import com.cloudm.sign.constant.Constants;
import com.cloudm.sign.enums.SecurityValidateEnum;
import com.cloudm.sign.utils.EncryptUtil;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Jay
 * @version v1.0
 * @description 对http接口进行安全控制
 * @date 2018-06-26 10:36
 */
public class HttpRequestValidateInterceptor extends HandlerInterceptorAdapter {



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            SecurityValidate securityValidate = ((HandlerMethod) handler).getMethodAnnotation(SecurityValidate.class);
            // 如果没有打注解表示该接口不需要进行特殊的安全机制保护
            if (securityValidate == null) {
                return super.preHandle(request, response, handler);
            }
            // 获取头中的请求时间戳
            String timeStamp = request.getHeader(Constants.TIMESTAMP);
            // 获取请求头中的sign
            String sign = request.getHeader(Constants.SIGN);
            // 校验参数
            if (StringUtils.isEmpty(timeStamp) || StringUtils.isEmpty(sign)) {
                throw new BusinessCheckFailException(SecurityValidateEnum.SECURITY_VALIDATE_ERROR.getKey(),
                        SecurityValidateEnum.SECURITY_VALIDATE_ERROR.getName());
            }
            // 获取请求的接口名称
            String methodName = request.getRequestURI();
            // 时间戳校验
            if (!this.checkTimeStamp(methodName, timeStamp)) {
                throw new BusinessCheckFailException(SecurityValidateEnum.SECURITY_VALIDATE_ERROR.getKey(),
                        SecurityValidateEnum.SECURITY_VALIDATE_ERROR.getName());
            }
            // 进行sign校验
            if (!this.securityValidate(request, sign, timeStamp)) {
                throw new BusinessCheckFailException(SecurityValidateEnum.SECURITY_VALIDATE_ERROR.getKey(),
                        SecurityValidateEnum.SECURITY_VALIDATE_ERROR.getName());
            }
        } catch (Exception e) {
            this.writeErrorResponse(response, e);
            return Boolean.FALSE;
        }
        return super.preHandle(request, response, handler);
    }



    /*------------------------------------------------------private method-----------------------------------------------------*/


    /**
     * 校验请求是否过期了
     *
     * @param methodName
     * @param timeStamp
     * @return
     */
    private boolean checkTimeStamp(String methodName, String timeStamp) {
        if (StringUtils.isEmpty(methodName) || StringUtils.isEmpty(timeStamp)) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "methodName不能为空");
        }
        // 比较时间戳
        long currentTimeStamp = System.currentTimeMillis() / 1000;
        long time = Long.valueOf(timeStamp);
        // 当前时间戳大于请求时间戳10s,则表示该请求过期
        if (currentTimeStamp - time > Constants.TEN_SECOND) {
            throw new BusinessCheckFailException(BaseErrorEnum.BNS_CHK_ERROR.getCode(), "过期的请求");
        }
        return Boolean.TRUE;
    }


    /**
     * 执行接口安全校验
     *
     * @param request
     * @param sign
     * @param timeStamp
     * @return
     */
    private boolean securityValidate(HttpServletRequest request, String sign, String timeStamp) {
        Enumeration enumeration = request.getParameterNames();
        SortedMap<String, String> sortedMap = new TreeMap<>();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String value = request.getParameter(key);
            sortedMap.put(key, value);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            stringBuilder.append(entry.getKey());
        }
        stringBuilder.append(timeStamp);
        // MD5运算
        String md5Str = EncryptUtil.getMD5Str(stringBuilder.toString());
        // 比较sign是否相等
        if (!StringUtils.isEmpty(md5Str) && md5Str.equals(sign)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    /**
     * 接口安全认证失败返回的response
     *
     * @param e
     */
    private Result writeResponse(Exception e) {
        Result result = new Result();
        result.setCode(SecurityValidateEnum.SECURITY_VALIDATE_ERROR.getKey());
        result.setSuccess(Boolean.FALSE);
        result.setDevMsg(e.getMessage());
        return result;
    }


    /**
     * 输出结果集
     *
     * @param response
     * @param e
     * @throws Exception
     */
    private void writeErrorResponse(HttpServletResponse response, Exception e) throws Exception {
        response.setHeader("contentType", "text/html; charset=utf-8");
        response.setContentType("text/html;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(new Gson().toJson(this.writeResponse(e)));
    }
}
