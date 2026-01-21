package com.example.whowroteit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Boîte à outils globale pour la gestion des threads.
 */
public class AppExecutors {

    // Nous créons un pool fixe de 4 threads.
    // "static" permet d'y accéder depuis n'importe quelle classe (ViewModel, etc.)
    public static final ExecutorService BACKGROUND = Executors.newFixedThreadPool(4);
}
