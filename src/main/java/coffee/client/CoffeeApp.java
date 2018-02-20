package coffee.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.Single;

public class CoffeeApp implements EntryPoint {

    @Override public void onModuleLoad() {
        Util.log("GWT+Dagger2 v" + System.getProperty("project.version") + " starting...");
        Bootstrapper bootstrapper = DaggerCoffeeApp_Bootstrapper.create();

        Button drip = new Button("Drip Coffee");
        drip.addClickHandler(e -> bootstrapper.dripCoffeeMaker().subscribe(CoffeeMaker::brew));
        RootPanel.get().add(drip);
    }

    @Singleton @Component(modules = BootstrapModule.class) interface Bootstrapper {
        @Named("drip") Single<CoffeeMaker> dripCoffeeMaker();
    }

    @Module(subcomponents = DripCoffeeComponent.class) static class BootstrapModule {
        @Provides @Singleton Single<DripCoffeeComponent> dripCoffeeComponent(DripCoffeeComponent.Builder b) {
            return Single.<DripCoffeeComponent>create(s -> GWT.runAsync(new RunAsyncCallback() {
                @Override public void onFailure(Throwable reason) { s.onError(reason); }
                @Override public void onSuccess() { s.onSuccess(b.build()); }
            })).cache();
        }
        @Provides @Singleton @Named("drip") Single<CoffeeMaker> produceDripCoffeeMaker(Single<DripCoffeeComponent> c) {
            return c.map(DripCoffeeComponent::maker);
        }
    }

    @Subcomponent(modules = DripCoffeeModule.class) interface DripCoffeeComponent {
        CoffeeMaker maker();
        @Subcomponent.Builder interface Builder {
            DripCoffeeComponent build();
        }
    }

    @Module static abstract class DripCoffeeModule {
        @Provides static Heater heater() { return new ElectricHeater(); }
        @Binds abstract Pump pump(Thermosiphon pump);
    }
}
