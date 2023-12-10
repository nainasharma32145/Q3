import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class Order {
    private static final Random random = new Random();

    private int cargoWeight;
    private String destination;

    public Order() {
        this.cargoWeight = random.nextInt(41) + 10; // Random cargo weight between 10 and 50 tons
        this.destination = random.nextBoolean() ? "Gotham" : "Atlanta"; // Random destination
    }

    public int getCargoWeight() {
        return cargoWeight;
    }

    public String getDestination() {
        return destination;
    }
}

class Ship {
    private int currentCargo;

    public Ship() {
        this.currentCargo = 0;
    }

    public void loadCargo(Order order) {
        currentCargo += order.getCargoWeight();
    }

    public void deliverCargo(int earnings) {
        // Simulate delivering cargo and earning money
        // You can add more logic here if needed
    }

    public void sendToMaintenance() {
        // Simulate sending the ship to maintenance
        // You can add more logic here if needed
    }

    public int getCurrentCargo() {
        return currentCargo;
    }
}

class WayneEnterprise {
    private static final int ORDER_COST = 1000;
    private static final int CANCELED_ORDER_PENALTY = 250;
    private static final int TARGET_EARNINGS = 1000000;

    private static int totalEarnings = 0;
    private static int totalOrdersDelivered = 0;
    private static int totalOrdersCanceled = 0;

    private static BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private static BlockingQueue<Ship> availableShips = new LinkedBlockingQueue<>();

    private static final Object lock = new Object();

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            availableShips.add(new Ship());
        }

        // Start shipping threads
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        Ship ship = availableShips.take();
                        Order order = orderQueue.take();
                        ship.loadCargo(order);

                        // Simulate shipping time
                        Thread.sleep(100);

                        ship.deliverCargo(ORDER_COST);
                        totalOrdersDelivered++;

                        synchronized (lock) {
                            totalEarnings += ORDER_COST;
                            if (totalEarnings >= TARGET_EARNINGS) {
                                System.out.println("Simulation completed!");
                                printResults();
                                System.exit(0);
                            }
                        }

                        availableShips.add(ship);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        // Start consumer threads
        for (int i = 0; i < 7; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        Order order = new Order();
                        orderQueue.put(order);

                        // Simulate order placement time
                        Thread.sleep(50);

                        synchronized (lock) {
                            if (totalEarnings >= TARGET_EARNINGS) {
                                System.out.println("Consumer thread terminating");
                                printResults();
                                System.exit(0);
                            }

                            if (orderQueue.size() > 1) {
                                totalOrdersCanceled++;
                                totalEarnings -= CANCELED_ORDER_PENALTY;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static void printResults() {
        System.out.println("Total orders delivered: " + totalOrdersDelivered);
        System.out.println("Total orders canceled: " + totalOrdersCanceled);
        System.out.println("Total earnings: $" + totalEarnings);
    }
}
