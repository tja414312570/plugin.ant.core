package com.yanan.framework.ant.process;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorThreadFactory implements ThreadFactory{
      private final ThreadGroup group;
      private final AtomicInteger threadNumber = new AtomicInteger(1);
      private final String namePrefix;
      

      ExecutorThreadFactory(ExecutorServer executorServer) {
          SecurityManager s = System.getSecurityManager();
          group = (s != null) ? s.getThreadGroup() :
                                Thread.currentThread().getThreadGroup();
          namePrefix = "executor-process-pool-"+
        		  				executorServer+
                                "-";
      }

      public Thread newThread(Runnable r) {
          Thread t = new Thread(group, r,
                                namePrefix + threadNumber.getAndIncrement(),
                                0);
          if (t.isDaemon())
              t.setDaemon(false);
          if (t.getPriority() != Thread.NORM_PRIORITY)
              t.setPriority(Thread.NORM_PRIORITY);
          return t;
      }

}