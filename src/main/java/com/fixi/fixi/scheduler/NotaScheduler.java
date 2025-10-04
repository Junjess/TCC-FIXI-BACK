package com.fixi.fixi.scheduler;

import com.fixi.fixi.service.AvaliacaoPlataformaService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotaScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AvaliacaoPlataformaService avaliacaoService;

    public NotaScheduler(AvaliacaoPlataformaService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    public void iniciar() {
        Runnable tarefa = () -> {
            System.out.println("🔄 Calculando notas mensais dos prestadores...");
            avaliacaoService.calcularNotasMensais();
        };

        // executa agora e repete a cada 60 dias
        scheduler.scheduleAtFixedRate(tarefa, 0, 60, TimeUnit.DAYS);
    }
}
