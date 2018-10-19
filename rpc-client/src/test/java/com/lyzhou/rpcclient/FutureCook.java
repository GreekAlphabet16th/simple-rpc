package com.lyzhou.rpcclient;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Future使用
 * @author zhouliyu
 * */
public class FutureCook {

    public static void main(String[] args){
        long startTime = System.currentTimeMillis();
        //1.网购厨具
        Callable<CookWare> onlineShopping = new Callable<CookWare>() {
            @Override
            public CookWare call() throws Exception {
                System.out.println("第一步：下单");
                System.out.println("第一步：等待送货");
                Thread.sleep(50); //送货时间5s
                System.out.println("第一步：快递送到");
                return new CookWare();
            }
        };
        FutureTask<CookWare> task = new FutureTask<>(onlineShopping);
        new Thread(task).start();
        //2.去超市购买食材
        try {
            Thread.sleep(30);//购买时间3s
            Ingredient ingredient = new Ingredient();
            System.out.println("第二步：食材到位");
            //3.用厨具烹饪
            if(!task.isDone()){
                System.out.println("第三步：厨具还没到位,等等");
            }
            CookWare cookWare = task.get();
            cook(cookWare, ingredient);
            System.out.println("第三步：厨具到位,开始展现厨艺");
            System.out.println("共用时：" + (System.currentTimeMillis()-startTime) + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    static void cook(CookWare cookWare, Ingredient ingredient){

    }

    //厨具类
    static class CookWare{}
    //食材类
    static class Ingredient{}

}
