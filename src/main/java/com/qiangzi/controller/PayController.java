package com.qiangzi.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.qiangzi.config.AliPayConfig;
import com.qiangzi.utils.AliPayTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by qiang on 2019/2/28.
 */
@Controller
public class PayController {

    @Autowired
    private AliPayTemplate aliPayClient;
    @Autowired
    private AliPayConfig aliPayConfig;

    @PostMapping("/queryPay")
    @ResponseBody
    public String queryPay(HttpServletRequest request){
        //商户订单号
        String wiDout_trade_no = request.getParameter("WIDout_trade_no");
        //支付宝交易号
        String wIDtrade_no = request.getParameter("WIDtrade_no");
        try {
           return aliPayClient.tradeQuery(wiDout_trade_no,wIDtrade_no);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
     return null;
    }

    @PostMapping("/aliPay")
    public void pay(HttpServletRequest request, HttpServletResponse response){
        //订单编号
        String wiDout_trade_no = request.getParameter("WIDout_trade_no");
        //订单名称
        String WIDsubject = request.getParameter("WIDsubject");
        //订单金额
        String WIDtotal_amount = request.getParameter("WIDtotal_amount");
        //描述
        String WIDbody = request.getParameter("WIDbody");
        try {
            String from = aliPayClient.webPay(wiDout_trade_no, WIDsubject, WIDtotal_amount, WIDbody, "2m", aliPayConfig.getProductCodeWeb());
            System.out.println(from);
            response.setContentType("text/html;charset=" + "UTF-8");
            response.getWriter().write(from);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @PostMapping("/aliPcPay")
    public void pcPay(HttpServletRequest request, HttpServletResponse response){
        //订单编号
        String wiDout_trade_no = request.getParameter("WIDout_trade_no");
        //订单名称
        String WIDsubject = request.getParameter("WIDsubject");
        //订单金额
        String WIDtotal_amount = request.getParameter("WIDtotal_amount");
        //描述
        String WIDbody = request.getParameter("WIDbody");
        try {
            String from = aliPayClient.pagePay(wiDout_trade_no, WIDsubject, WIDtotal_amount, WIDbody, "2m", aliPayConfig.getProductCodePage());
            System.out.println(from);
            response.setContentType("text/html;charset=" + "UTF-8");
            response.getWriter().write(from);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/return_url")
    public String return_url(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        System.out.println("requestURI = "+requestURI);
        String queryString = request.getQueryString();
        System.out.println("queryString = "+queryString);

        return "/return_url.html";
    }

    @PostMapping("/notify_url")
    public void  notify_url(HttpServletRequest request , HttpServletResponse response){
        String requestURI = request.getRequestURI();
        System.out.println("requestURI = "+requestURI);
        Map<String,String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            System.out.println("name = "+name +"  value = "+valueStr);
            params.put(name, valueStr);
        }
        //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
        //商户订单号
        response.setContentType("text/html;charset=" + "UTF-8");
        try {

            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号

            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");

            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
            //计算得出通知验证结果
            //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
            boolean verify_result = AlipaySignature.rsaCheckV1(params, aliPayConfig.getAliPayPublicKey(), aliPayConfig.getCharset(), "RSA2");
            System.out.println("verify_result="+verify_result);
            if(verify_result){//验证成功
                //////////////////////////////////////////////////////////////////////////////////////////
                //请在这里加上商户的业务逻辑程序代码

                //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

                if(trade_status.equals("TRADE_FINISHED")){
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    //如果有做过处理，不执行商户的业务程序

                    //注意：
                    //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                    //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                } else if (trade_status.equals("TRADE_SUCCESS")){
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    //如果有做过处理，不执行商户的业务程序

                    //注意：
                    //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。


                }else if (trade_status.equals("TRADE_CLOSED")){
                    //交易关闭


                } else if (trade_status.equals("WAIT_BUYER_PAY")){
                    //交易创建


                }

                //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
                //请不要修改或删除
                response.getWriter().write("success");
                response.getWriter().flush();
                response.getWriter().close();
                //////////////////////////////////////////////////////////////////////////////////////////
            }else{//验证失败
                response.getWriter().write("fail");
                response.getWriter().flush();
                response.getWriter().close();
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                response.getWriter().write("fail");
                response.getWriter().flush();

            }catch (Exception e1){
                e1.printStackTrace();
            }

        }finally {
            try {

                response.getWriter().close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
