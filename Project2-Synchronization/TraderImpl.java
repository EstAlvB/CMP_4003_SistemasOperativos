import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class TraderImpl implements Trader {

   private Grain specialityGrain;
   private Order stock;
   private int surplus = 0;
   private final int maxWait = 50;// true, so locks favor granting access to the longest-waiting thread
   private final ReentrantLock lock = new ReentrantLock(true);
   private final ReentrantLock stockLock = new ReentrantLock(true);

    public TraderImpl(Grain specialty) {
      this.specialityGrain = specialty;
      stock = new Order();
    }

    public Order getAmountOnHand() {
        //getAmountOnHand_String();
        //System.out.println(getAmountOnHand_String());
        return stock;
    }

    public void get(Order order) throws InterruptedException {
         //printEverybodyStock();
         while (lock.isLocked()) {
            Thread.sleep(100);
         }lock.lock();
    /*     try {
            System.out.println("Order ");
            int timesWaiting = 0;
            while (stock.get(this.specialityGrain)< order.get(this.specialityGrain)) {
            //System.out.print("Order " + order.get(this.specialityGrain)
            //
            */
    }

    public void swap(Grain what, int amt) throws InterruptedException {

    }

    public void deliver(int amt) throws InterruptedException {

    }
    
}

