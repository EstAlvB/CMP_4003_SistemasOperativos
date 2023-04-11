import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class TraderImpl implements Trader {

   private Grain specialityGrain;
   private Order stock;
   private static final int maxWaitTime = 50;
   private final ReentrantLock lock = new ReentrantLock(true);
   private final ReentrantLock stockLock = new ReentrantLock(true);

    public TraderImpl(Grain grain) {
      this.specialityGrain = grain;
      stock = new Order();
    }

    @Override
    public Order getAmountOnHand() {
      getAmountOnHand_String();
      return stock;
    }

    public void get(Order order) throws InterruptedException {
         //printEverybodyStock();
         while (lock.isLocked()) {
         Thread.sleep(100);
         }
         lock.lock();
         try {
            System.out.println("Order ");
            int timesWaiting = 0;
            while (stock.get(this.specialityGrain)
                        < order.get(this.specialityGrain)) {   
                  //System.out.print("Order " + order.get(this.specialityGrain)
                  // + " of " + specialityGrain.toString() + ": ");
                  //System.out.println("Not enough speciality yet");
                  Thread.sleep(50);
                  if (timesWaiting++ > maxWaitTime) {
                     Thread.currentThread().interrupt();
                     Thread.currentThread().join();
                     //System.out.println("maxWaitTime on GET1");
                     return;
                  }  
            }
            //System.out.println("Successfully deliever "
            // + order.get(specialityGrain)
            // + " of speciality "
            // + this.specialityGrain.toString());
            //TODO
            //changeStock(specialityGrain, -order.get(specialityGrain));
            ArrayList<Grain> grains = new ArrayList(Arrays.asList(Grain.values()));
            
            grains.remove(this.specialityGrain);
            for (Grain grain : grains) {
            
                  timesWaiting = 0;
                  int amt = order.get(grain) - stock.get(grain);
                  boolean bool = true;
                  while (stock.get(grain) < order.get(grain) && bool) {
                        if (stock.get(specialityGrain) >= amt) {
                              /*System.out.println("Trying to swap "
                                    + specialityGrain.toString()
                                    + " for " + grain.toString());*/
                              P3.specialist(grain).swap(specialityGrain, amt);
                              changeStock(grain, amt);
                              changeStock(specialityGrain, -amt);
                              //TODO
                              //changeStock(grain, -order.get(grain));
                              bool = false;
                        } else {
                               Thread.sleep(100);
                               if (timesWaiting++ > maxWaitTime) {
                                     Thread.currentThread().interrupt();
                                     Thread.currentThread().join();
                                     //System.out.println("maxWaitTime on GET2");
                                     return;
                               }
                        }
                  }
            }
               // TODO
               // quedan productos netos porque se entregan al cervecero
               // apenas esten disponibles. Esto causa un problema al
               // final porque el sistema no registra que se ha entregado
               // si la entrega no es completa.
               // por ello se agregan estos productos entregados a
               // medias, a las bodegas del trader.
               grains.add(specialityGrain);
               for (Grain grain : grains) {
                     changeStock(grain, -order.get(grain));
               }
          } finally {
               lock.unlock();
          }
                
    }

    @Override
    public  synchronized void swap(Grain what, int amt) throws InterruptedException {
            //printEverybodyStock();
            // si tengo menos granos de especialidad do los que me piden
            // cambiar, espero al supplier. Espero al supplier un maximo
            // de tiempo.
            int maxTimesWaiting = 0;
            while (stock.get(this.specialityGrain) < amt) {
               Thread.sleep(50);
               if (maxTimesWaiting++ > maxWaitTime) {
                   Thread.currentThread().interrupt();
                   Thread.currentThread().join();
                   //System.out.println("maxWaitTime on SWAP");
                   return;
               }
            }
            changeStock(this.specialityGrain, -amt);
            changeStock(what, amt);
            //System.out.println("Succesfull Swap Transaction");
    }
    
    @Override
    public void deliver(int amt) throws InterruptedException {
            changeStock(this.specialityGrain, amt);
    }
    
    private void getAmountOnHand_String() {
         String string;
         string = "From trader specialized in "
               + specialityGrain.toString()
               + ", stock:\n"
               + "Barley: " + stock.get(Grain.BARLEY) + ", "
               + "Corn: " + stock.get(Grain.CORN) + ", "
               + "Rice: " + stock.get(Grain.RICE) + ", "
               + "Wheat: " + stock.get(Grain.WHEAT);
         System.out.println(string);
    }
    
    private void changeStock(Grain grain, int amt) throws InterruptedException {
         stockLock.lock();
         try {
             /*System.out.println("changeStock: "
             + grain.toString() + ", amt: "
             + amt);*/
             stock.change(grain, amt);
         } finally {
             //printEverybodyStock();
             stockLock.unlock();
         }
    }
   
}

