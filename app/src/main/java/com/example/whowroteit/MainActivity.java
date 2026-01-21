package com.example.whowroteit;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.content.Context;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;
    private BookViewModel mViewModel; // Référence vers le ViewModel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Initialisation des composants UI
        mBookInput = findViewById(R.id.bookInput);
        mTitleText = findViewById(R.id.titleText);
        mAuthorText = findViewById(R.id.authorText);
        Button searchButton = findViewById(R.id.searchButton);

        // 2. Récupération du ViewModel via le Provider
        // Cela permet au ViewModel de survivre à la rotation de l'écran.
        mViewModel = new ViewModelProvider(this).get(BookViewModel.class);

        // 3. Mise en place des Observers (Ecouteurs de données)
        // Dès que le ViewModel reçoit une réponse de l'API, l'UI se met à jour ici.
        mViewModel.getTitleResult().observe(this, title -> mTitleText.setText(title));
        mViewModel.getAuthorResult().observe(this, author -> mAuthorText.setText(author));

        // Observation de l'état (Chargement / Erreur / Prêt)
        mViewModel.getStatusMessage().observe(this, status -> mTitleText.setText(status));

        // 4. Gestion du clic sur le bouton
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBooks(v);
            }
        });
    }

    /**
     * Méthode appelée lors du clic sur le bouton de recherche.
     */
    public void searchBooks(View view) {
        String queryString = mBookInput.getText().toString();

        // 1. Masquer le clavier
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // 2. Vérifier l'état du réseau
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = false;

        if (connMgr != null) {
            // On récupère le réseau actif
            Network network = connMgr.getActiveNetwork();
            // On récupère les capacités de ce réseau
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(network);

            // On vérifie si le réseau a accès à Internet
            isConnected = capabilities != null &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }

        // 3. Logique de décision
        if (isConnected && queryString.length() != 0) {
            // Tout est OK : on lance la recherche
            mViewModel.startSearch(queryString);
        } else {
            mAuthorText.setText("");
            // Gestion des cas d'erreur
            if (queryString.length() == 0) {
                mTitleText.setText(R.string.no_search_term);
            } else {
                mTitleText.setText(R.string.no_network);
            }
        }
    }
}