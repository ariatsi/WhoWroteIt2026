package com.example.whowroteit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class BookViewModel extends ViewModel {
    private final MutableLiveData<String> titleResult = new MutableLiveData<>();
    private final MutableLiveData<String> authorResult = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>("Prêt");

    public LiveData<String> getTitleResult() { return titleResult; }
    public LiveData<String> getAuthorResult() { return authorResult; }
    public LiveData<String> getStatusMessage() { return statusMessage; }


    public void startSearch(String query) {
        statusMessage.setValue("Chargement...");

        AppExecutors.BACKGROUND.execute(() -> {
            // 1. Faire la requête via NetworkUtils
            String jsonString = NetworkUtils.getBookInfo(query);

            if (jsonString != null) {
                // 2. Analyser le JSON (appelle la méthode parseJson que nous avons créée)
                // Note : parseJson va elle-même faire les postValue() pour le titre et l'auteur
                parseJson(jsonString);
            } else {
                statusMessage.postValue("Erreur de connexion");
            }
        });
    }

    private void parseJson(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            int i = 0;
            String title = null;
            String authors = null;

            // On parcourt les résultats jusqu'à trouver un livre qui a un TITRE et un AUTEUR
            while (i < itemsArray.length() && (authors == null || title == null)) {
                // On récupère le premier livre (index 0)
                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                try {
                    title = volumeInfo.getString("title");
                    authors = volumeInfo.getJSONArray("authors").getString(0);
                } catch (Exception e) {
                    // Si un champ manque, on continue la boucle vers le livre suivant
                    title = null;
                    authors = null;
                }
                i++;
            }

            if (title != null && authors != null) {
                // Mise à jour de l'UI via postValue (indispensable ici !)
                statusMessage.postValue("Résultat trouvé :");
                titleResult.postValue(title);
                authorResult.postValue(authors);
            } else {
                statusMessage.postValue("Aucun résultat exploitable");
            }
        } catch (Exception e) {
            statusMessage.postValue("Erreur lors de la réception des données");
        }
    }


}
