package pl.raaadziu.coinsservice;

import java.time.Instant;
import java.util.ArrayList;

class Worker extends Thread
{
    private boolean toExit = false;
    private boolean stopped = false;
    private ArrayList<CryptoCore> cores;

    Worker(ArrayList<CryptoCore> cores)
    {
        this.cores = cores;
    }

    @Override
    public void run()
    {
        Instant time = Instant.now().plusMillis(100);

        while(true)
        {
            while(Instant.now().isBefore(time))
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    //
                }
            }

            time = time.plusSeconds(5);

            for (CryptoCore c : cores) {
                c.tryDoTasks();
            }

            if (toExit)
            {
                stopped = true;
                break;
            }
        }
    }

    boolean isStopped()
    {
        return stopped;
    }

    void goToExit()
    {
        toExit = true;
    }
}