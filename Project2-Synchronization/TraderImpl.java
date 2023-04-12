import java.util.ArrayList;
import java.util.Arrays;
//Libreria para manejo de bloqueos y sincronizacion utilizando hilos en java
import java.util.concurrent.locks.*;

public class TraderImpl implements Trader {

   //Instanciamos los objetos Grain y Order para el tipo de grano y Existencias
   private Grain grainSpec;
   private Order orderStock;
   //Tiempo maximo de espera
   private final int maxTime = 50;
   //Controlar que los hilos intenten acceder al mismo recurso al mismo tiempo
   private final ReentrantLock rLock = new ReentrantLock(true);
   private final ReentrantLock lockInStock = new ReentrantLock(true);

   //Constructor para el iniciarlizar las nuevas intancias
    public TraderImpl(Grain grain) {
      this.grainSpec = grain;
      orderStock = new Order();
    }

    //Metodo para obtener la cantidad disponible
    @Override
    public Order getAmountOnHand() {
      getAmountOnHand_String();
      return orderStock;
    }

    //Metodo para obtener la cantidad solicitada
    public void get(Order order) throws InterruptedException {
         // Controlamos que mientras este bloqueado espere
         while (rLock.isLocked()) {   
         Thread.sleep(100);
         }
         rLock.lock();
         try {
            System.out.println("Order ");
            int waitTime = 0;
            while (orderStock.get(this.grainSpec)
                        < order.get(this.grainSpec)) {   
                  Thread.sleep(50);
                  //So el tiempo de espera sobrepasa al maximo interumpe al hilo y se une al hilo actual
                  if (waitTime++ > maxTime) {
                     Thread.currentThread().interrupt();
                     Thread.currentThread().join();
                     return;
                  }  
            }
            //Se crea una lista de los granos disponibles
            ArrayList<Grain> gainList = new ArrayList(Arrays.asList(Grain.values()));
            
            //Se remueve el grano especificado
            gainList.remove(this.grainSpec);
            for (Grain grain : gainList) {
                  waitTime = 0;
                  //Se almacena la cantidad de granos que no estan disponibles
                  int amt = order.get(grain) - orderStock.get(grain);
                  boolean bool = true;
                  while (orderStock.get(grain) < order.get(grain) && bool) {
                        //si el stock disponible del grano especificado es mayor o igual al requerido llamamos al metodo P3 para el intercambio
                        if (orderStock.get(grainSpec) >= amt) {
                              P3.specialist(grain).swap(grainSpec, amt);
                              //Actualizamos Stock del grano actual
                              changeStock(grain, amt);
                              //Actualizamos el Stock del grano solicitado
                              changeStock(grainSpec, -amt);
                              bool = false;
                        //Controlamos el tiempo de espera y evitamos tiempos infinitos de espera
                        } else {
                               Thread.sleep(100);     
                               //Si el tiempo de espera supera al tiempo maximo el hilo se interrumpe y se une al hilo actual
                               if (waitTime++ > maxTime) {
                                     Thread.currentThread().interrupt();
                                     Thread.currentThread().join();
                                     return;
                               }
                        }
                  }
            }
               //Se agrega el grano a la lista de granos disponibles
               gainList.add(grainSpec);
               for (Grain grain : gainList) {
                  //De actualiza la cantidad de existencias de los granos
                     changeStock(grain, -order.get(grain));
               }
               //Se desbloquea el recurso
          } finally {
               rLock.unlock();
          }
                
    }


    //Metodo para realizar el intercambio del grano
    @Override
    public  synchronized void swap(Grain what, int amt) throws InterruptedException {
            int maxWaitTime = 0;
            //Se espera a que exista suficiente cantidad de granos
            while (orderStock.get(this.grainSpec) < amt) {
               Thread.sleep(50);
               //Si espera demasiado interrumpe al hilo y se une al hilo actual
               if (maxWaitTime++ > maxTime) {
                   Thread.currentThread().interrupt();
                   Thread.currentThread().join();
                   return;
               }
            }
            //Se actualiza la cantidad de granos actual
            changeStock(this.grainSpec, -amt);
            //Se aumenta la cantidad de granos intercambiados
            changeStock(what, amt);
    }
    
    //Motificar el stock del grano
    @Override
    public void deliver(int amt) throws InterruptedException {
            changeStock(this.grainSpec, amt);
    }
    
    //Metodo para impresion de la cantidad actual de existencias de granos
    private void getAmountOnHand_String() {
         String string;
         string = "From trader specialized in "
               + grainSpec.toString()
               + ", stock:\n"
               + "Barley: " + orderStock.get(Grain.BARLEY) + ", "
               + "Corn: " + orderStock.get(Grain.CORN) + ", "
               + "Rice: " + orderStock.get(Grain.RICE) + ", "
               + "Wheat: " + orderStock.get(Grain.WHEAT);
         System.out.println(string);
    }

    //Metodo para intercambiar un grano espeficico considerando una cantidad que se aumenta o se resta. 
    private void changeStock(Grain grain, int amt) throws InterruptedException {
       //Se bloquea para asegurar que otro hilo no realice modificaciones 
         lockInStock.lock();
         try {
             orderStock.change(grain, amt);
         } 
         //Al realizar la modificacion al inventario se desbloquea 
         finally {
             lockInStock.unlock();
         }
    }
   
}

