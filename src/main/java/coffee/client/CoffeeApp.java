package coffee.client;

import static com.google.common.util.concurrent.Futures.addCallback;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.producers.ProducerModule;
import dagger.producers.Produces;
import dagger.producers.Production;
import dagger.producers.ProductionComponent;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import javax.inject.Named;

public class CoffeeApp implements EntryPoint {

    @Override public void onModuleLoad() {
        Util.log("GWT+Dagger2 v" + System.getProperty("project.version") + " starting...");
        Bootstrapper bootstrapper = DaggerCoffeeApp_Bootstrapper.builder().build();

        Button drip = new Button("Drip Coffee");
        drip.addClickHandler(e -> addCallback(bootstrapper.dripCoffeeMaker(), new FutureCallback<CoffeeMaker>() {
            public void onSuccess(@Nullable CoffeeMaker result) { result.brew(); }
            public void onFailure(Throwable t) { GWT.log("error producing " + CoffeeMaker.class.getName(), t); }
        }));
        RootPanel.get().add(drip);
    }

    @ProductionComponent(modules = BootstrapModule.class) interface Bootstrapper {
        DripCoffeeComponent dripCoffeeComponent();
        @Named("drip") ListenableFuture<CoffeeMaker> dripCoffeeMaker();
    }

    @ProducerModule static class BootstrapModule {
        @Provides @Production Executor executor() { return MoreExecutors.directExecutor(); }
        @Produces @Named("drip") ListenableFuture<CoffeeMaker> produceDripCoffeeMaker(Bootstrapper bootstrapper) {
            SettableFuture<CoffeeMaker> future = SettableFuture.create();
            GWT.runAsync(new RunAsyncCallback() {
                @Override public void onFailure(Throwable reason) { future.setException(reason); }
                @Override public void onSuccess() { future.set(bootstrapper.dripCoffeeComponent().maker()); }
            });
            return future;
        }
    }

    @Subcomponent(modules = DripCoffeeModule.class) interface DripCoffeeComponent {
        CoffeeMaker maker();
    }

    @Module(includes = PumpModule.class) static class DripCoffeeModule {
        @Provides Heater provideHeater() { return new ElectricHeater(); }
    }

    @Module abstract static class PumpModule {
        @Binds abstract Pump providePump(Thermosiphon pump);
    }
}
