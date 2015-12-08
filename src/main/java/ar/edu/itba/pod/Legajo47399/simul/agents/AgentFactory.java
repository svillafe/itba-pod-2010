package ar.edu.itba.pod.Legajo47399.simul.agents;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.Factory.FactoryBuilder;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class AgentFactory {

	public static Factory createFactory(String name,
			List<ResourceCuantity> using, ResourceCuantity producing,
			Integer time, TimeUnit unit) {
		FactoryBuilder fb = Factory.named(name);
		fb.using(using.get(0).getCant(), using.get(0).getRecurso());

		for (int i = 1; i < using.size(); i++) {
			fb.and(using.get(i).getCant(), using.get(i).getRecurso());
		}
		fb = fb.producing(producing.getCant(), producing.getRecurso());
		return fb.every(time, unit).build();

	}

	public static SimpleConsumer createConsumer(String name,
			ResourceCuantity consuming, Integer time, TimeUnit unit) {
		return SimpleConsumer.named(name).consuming(consuming.getCant())
				.of(consuming.getRecurso()).every(time, unit).build();

	}

	public static SimpleProducer createProducer(String name,
			ResourceCuantity producing, Integer time, TimeUnit unit) {
		return SimpleProducer.named(name).producing(producing.getCant())
				.of(producing.getRecurso()).every(time, unit).build();
	}
}
