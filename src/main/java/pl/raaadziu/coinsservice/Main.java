package pl.raaadziu.coinsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.raaadziu.coinsservice.DTOs.CryptoConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class Main implements CommandLineRunner
{
    private SqlApi sql;
    @Autowired
    public Main(SqlApi sqlApi)
    {
        this.sql = sqlApi;
    }

    @Override
    public void run(String... strings) throws SqlApiException {
        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ArrayList<CryptoCore> cores = new ArrayList<>();

        List<CryptoConfig> configs = CryptoSqlApi.getCryptoConfig(sql);
        root.info("READ CONFIG");

        //initialize all structures
        for (CryptoConfig t : configs) {
            root.info(" CONFIG LINE " + t);
            Wallet w = new Wallet(t.getCoreIP(), t.getCorePort(), t.getCoreUser(), t.getCorePassword(), t.getDepositConfirmations(), t.getCID());
            CryptoSqlApi api = new CryptoSqlApi(sql, t.getCID());
            CryptoCore core = new CryptoCore(w, t.getAccountFilter(), api);
            cores.add(core);
        }

        Worker work = new Worker(cores);
        work.start();

        // worker starts in separate thread
        // this thread is waiting for exit command to safe exit
        while(true)
        {
            Scanner scanner=new Scanner(System.in);
            System.out.println("Enter cmd ...");
            String temp=scanner.nextLine();

            if (temp.equals("exit"))
            {
                System.out.println("Waiting for exit ...");
                work.goToExit();
                while(!work.isStopped())
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        //
                    }
                }
                break;
            }
        }

    }
}
