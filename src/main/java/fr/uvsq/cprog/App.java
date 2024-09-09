package fr.uvsq.cprog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * The main class.
 */
public class App {
    /**
     * The application root.
     */
    private static String racineRelative;

    /**
     * The application root in the machine.
     */
    private static String racine;
    /**
     * The path of current file name of copy or cute.
     */
    private static Path fileInUsage;
    /**
     * The current file name of copy or cut.
     */
    private static String fileInUsageName;
    /**
     * The copy or cut action variable.
     */
    private static Integer typeUsage;
    
    /**
     * The main method.
     *
     * @param args cli arguments
     * @throws IOException if an I/O error arise reading the file to analyze
     */
    
    public static void main(String[] args) throws  IOException {
        // Définissons les chemins
        racineRelative = System.getProperty("user.dir") + File.separator + "root";
        racine = racineRelative;

        initialiserRepertoire(racineRelative);

        System.out.print("\u001B[33m\nTapez 'help' pour obtenir de l'aide !!!!!! \u001B[0m \n\n");
        afficherContenuRepertoireCourant();

        Scanner scanner = new Scanner(System.in);
        String ner = "";
        String commande;
        String nom;

        do {
            System.out.print("\nTapez une commande : ");
            String entreeUtilisateur = scanner.nextLine().trim();

            String[] commandes = entreeUtilisateur.split("\\s+");

            // Obtenons NER, commande et nom
            if (estNumeric(commandes[0])) {
                ner = commandes[0];
                commande = commandes.length >= 2 ? commandes[1] : "bad request";
                if (commandes.length > 3) {
                    nom = commandes[2];
                    StringBuilder tmp = new StringBuilder(nom);
                    for (int i = 3; i < commandes.length; i++) {
                        tmp.append(" ").append(commandes[i]);
                    }
                    nom = tmp.toString();
                } else {
                    nom =  commandes.length == 3 ? commandes[2] : null;
                }
            } else {
                commande = commandes[0];
                nom = commandes.length == 2 ? commandes[1] : null;
            }
            System.out.println("\n");
            //if
            // Traitons la commande
            switch (commande.toLowerCase()) {
                case "past":
                    past();
                    break;
                case ".":
                    naviguer(ner);
                    break;
                case "+":
                    ajouterTexteAnnotation(ner, nom);
                    break;
                case "-":
                    retirerTexteAnnotation(ner);
                    break;
                case "..":
                    remonterDossierParent();
                    break;
                case "find":
                    trouverFichier(nom);
                    break;
                case "copy":
                    copierElement(ner);
                    break;
                case "cut":
                    couperElement(ner);
                    break;
                case "mkdir":
                    creeSousRepertoire(nom);
                    break;
                case "visu":
                    visualiserFichier(ner);
                    break;
                case "ls":
                    afficherContenuRepertoireCourant();
                    break;
                case "rm":
                    supprimerFichier(ner);
                    break;
                case "exit":
                    System.out.println("Au revoir !");
                    System.exit(0);
                    break;
                case "help":
                    afficherAide();
                    break;
                case "pwd":
                    afficherPositionActuelle();
                    break;
                default:
                    System.out.println("\u001B[31mCommande non reconnue."
                            + " Vérifiez le format de la commande. "
                            + "Tapez 'help' pour obtenir de l'aide. \u001B[0m");
            }

            afficherContenuRepertoireCourant();

        } while (!commande.equalsIgnoreCase("exit"));
        scanner.close();
    }

    private static void initialiserRepertoire(String cheminRepertoire) {
        // Créons le répertoire si nécessaire
        File racineDir = new File(cheminRepertoire);
        if (!racineDir.exists()) {
            if (racineDir.mkdir()) {
                System.out.println("Répertoire créé : " + cheminRepertoire);
            } else {
                System.err.println("Impossible de créer le répertoire : " + cheminRepertoire);
                System.exit(1);
            }
        }
        // Créons le fichier d'index s'il n'existe pas
        File fichierIndex = new File(cheminRepertoire + File.separator + "notes.txt");
        if (!fichierIndex.exists()) {
            try {
                if (fichierIndex.createNewFile()) {
                    System.out.println("Fichier d'index créé : " + fichierIndex.getAbsolutePath());
                } else {
                    System.err.println("Impossible de créer le fichier d'index : "
                            + fichierIndex.getAbsolutePath());
                    System.exit(1);
                }
            } catch (IOException e) {
                System.err.println("Une erreur est survenu");
            }
        }
    }

    private static void afficherContenuRepertoireCourant() throws IOException {
        File repertoireFile = new File(racineRelative);
        if (!Files.exists(Path.of(racineRelative + File.separator + "notes.txt"))) {
            File fichierIndex = new File(racineRelative + File.separator + "notes.txt");
            new BufferedWriter(new FileWriter(fichierIndex));
        }
        File[] contenu = repertoireFile.listFiles();

        if (contenu != null && contenu.length > 0) {
            System.out.println("Contenu du répertoire : "
                    + "NER | Nom du fichier | Annotation | Chemin d'accès \n");
            List<String> lignesIndex = lireFichierIndex();
            //System.out.println(lignesIndex);
            int ner = 0;
            for (File fichier : contenu) {
                String nomFichier = fichier.getName();
                String annotation = "";
                // Récupérer l'annotation du fichier depuis le fichier d'index
                for (String ligne : lignesIndex) {
                    String[] elements = ligne.split(",");
                    if (elements.length == 3 && elements[1].equals(nomFichier)) {
                        annotation = elements[2];
                    }
                }
                System.out.println("---------------------------------"
                        + "-------------------------------------------------"
                        + "------------------------------");
                System.out.println("\u001B[32m"
                        + "[" + ner + "]"
                        + "\u001B[0m | \u001B[34m"
                        + nomFichier + "\u001B[0m | \u001B[36m"
                        + annotation + "\u001B[0m | \u001B[35m"
                        + extractRoot(fichier.getAbsolutePath())
                        + "\u001B[0m");
                ner++;
            }
            // Mettons à jour le fichier d'index avec les informations du répertoire
            mettreAjourFichierIndex(contenu, lignesIndex);
        } else {
            System.out.println("Le répertoire est vide.");
        }
    }

    private static void mettreAjourFichierIndex(File[] contenu, List<String> lignesIndex) {
        // Ajoutons ou mettons à jour les entrées du fichier d'index
        int ner = 0;
        for (File fichier : contenu) {
            String nomFichier = fichier.getName();
            boolean fichierExisteDansIndex = false;

            for (String ligne : lignesIndex) {
                String[] elements = ligne.split(",");
                if (elements[1].equals(nomFichier)) {
                    fichierExisteDansIndex = true;
                    break;
                }
            }

            if (!fichierExisteDansIndex) {
                // Ajoutons une nouvelle entrée pour le fichier
                lignesIndex.add((ner) + "," + nomFichier + "," + " ");
            }
            ner++;
        }

        // Écrivons les modifications dans le fichier d'index
        ecrireFichierIndex(lignesIndex);
    }

    private static List<String> lireFichierIndex() {
        List<String> lignes = new ArrayList<>();
        File fichierIndex = new File(racineRelative + File.separator + "notes.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(fichierIndex))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                lignes.add(ligne);
            }
        } catch (IOException e) {
            System.err.println("Une erreur est survenu");
        }

        return lignes;
    }

    private static void ecrireFichierIndex(List<String> lignes) {
        File fichierIndex = new File(racineRelative + File.separator + "notes.txt");
        //System.out.println(lignes);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fichierIndex))) {
            for (String ligne : lignes) {
                writer.write(ligne);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Une erreur est survenu");
        }
    }

    private static void naviguer(String ner) {
        if (ner != null) {
            try {
                int nerIndex = Integer.parseInt(ner);
                File repertoireFile = new File(racineRelative);
                File[] contenu = repertoireFile.listFiles();

                if (contenu != null && nerIndex >= 0 && nerIndex <= contenu.length) {
                    File fichierSelectionne = contenu[nerIndex];
                    if (fichierSelectionne.isDirectory()) {
                        racineRelative = fichierSelectionne.getAbsolutePath();
                        System.out.println("Vous avez change de repertoire avec success");
                    } else {
                        System.out.println("Le numéro associé à un "
                                + "répertoire doit pointer vers un répertoire.");
                    }
                } else {
                    System.out.println("Numéro associé invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("NER doit être un nombre entier. Réessayez.");
            }
        } else {
            System.out.println("NER non spécifié. Réessayez.");
        }
    }

    private static void supprimerFichier(String ner) {
        if (ner != null) {
            try {
                int nerIndex = Integer.parseInt(ner);
                File repertoireFile = new File(racineRelative);
                File[] contenu = repertoireFile.listFiles();
                if (contenu != null && nerIndex >= 0 && nerIndex <= contenu.length) {
                    File fichieraSupprimer = contenu[nerIndex];
                    if (fichieraSupprimer.isFile()) {
                        if (fichieraSupprimer.delete()) {
                            // Mettons à jour le fichier d'index après la suppression
                            List<String> lignesIndex = lireFichierIndex();
                            supprimerLigneIndex(nerIndex, lignesIndex);
                            //ecrireFichierIndex(lignesIndex);
                            mettreAjourFichierIndex(contenu, lignesIndex);

                            System.out.println("Fichier supprimé avec succès !");
                            //afficherContenuRepertoireCourant();
                        } else {
                            System.out.println("Échec de la suppression du fichier.");
                        }
                    } else {
                        System.out.println("Le numéro associé doit pointer vers un fichier.");
                    }
                } else {
                    System.out.println("Numéro associé invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("NER doit être un nombre entier. Réessayez.");
            }
        } else {
            System.out.println("NER non spécifié. Réessayez.");
        }
    }

    private static void supprimerLigneIndex(int nerIndex, List<String> lignesIndex) {
        for (Iterator<String> iterator = lignesIndex.iterator(); iterator.hasNext(); ) {
            String ligne = iterator.next();
            String[] elements = ligne.split(",");
            if (elements.length == 3) {
                int index = Integer.parseInt(elements[0]);
                if (index == nerIndex) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private static void afficherPositionActuelle() {
        System.out.println("Position actuelle dans l'arborescence : "
                + extractRoot(racineRelative));
    }

    private static void copierElement(String ner) {
        if (ner != null) {
            try {
                int nerIndex = Integer.parseInt(ner);
                File repertoireFile = new File(racineRelative);
                File[] contenu = repertoireFile.listFiles();

                if (contenu != null && nerIndex >= 0 && nerIndex <= contenu.length) {
                    File elementaCopier = contenu[nerIndex];
                    if (elementaCopier.exists()) {
                        if (elementaCopier.isFile()) {
                            fileInUsage = elementaCopier.toPath();
                            fileInUsageName = elementaCopier.getName();
                            typeUsage = 0;
                            System.out.println("Élément copié avec succès !");
                        } else {
                            System.out.println("Le numéro associé doit pointer vers un fichier.");
                        }
                    } else {
                        System.out.println("L'élément à copier n'existe pas.");
                    }
                } else {
                    System.out.println("Numéro associé invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("NER doit être un nombre entier. Réessayez.");
            }
        } else {
            System.out.println("NER non spécifié. Réessayez.");
        }
    }

    private static void couperElement(String ner) {
        if (ner != null) {
            try {
                int nerIndex = Integer.parseInt(ner);
                File repertoireFile = new File(racineRelative);
                File[] contenu = repertoireFile.listFiles();

                if (contenu != null && nerIndex >= 0 && nerIndex <= contenu.length) {
                    File elementaCouper = contenu[nerIndex];
                    if (elementaCouper.exists()) {
                        if (elementaCouper.isFile()) {
                            fileInUsage = elementaCouper.toPath();
                            fileInUsageName = elementaCouper.getName();
                            typeUsage = 1;
                            System.out.println("Élément coupé avec succès !");
                        } else {
                            System.out.println("Le numéro associé doit pointer vers un fichier.");
                        }
                    } else {
                        System.out.println("L'élément à couper n'existe pas.");
                    }
                } else {
                    System.out.println("Numéro associé invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("NER doit être un nombre entier. Réessayez.");
            }
        } else {
            System.out.println("NER non spécifié. Réessayez.");
        }
    }

    private static void past() throws IOException {
        if (fileInUsage != null && typeUsage != null) {
            File element = new File(racineRelative + File.separator + fileInUsageName);
            if (element.exists()) {
                element = new File(racineRelative + File.separator + "-copy_" + fileInUsageName);
            }
            if (typeUsage == 0) {
                Files.copy(fileInUsage, element.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(fileInUsage, element.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Vous avez coller un element dans ce repertoire");
        } else {
            System.out.println("Vous n'avez aucun element a coller");
        }
    }

    private static void trouverFichier(String nomFichier) {
        System.out.println("Recherche du fichier : " + nomFichier);

        File repertoireCourant = new File(racineRelative);
        List<File> fichiersTrouves = rechercherFichier(repertoireCourant, nomFichier);

        if (fichiersTrouves.isEmpty()) {
            System.out.println("Aucun fichier trouvé avec le nom : "
                    + nomFichier);
        } else {
            System.out.println("Fichiers trouvés : ");
            for (File fichier : fichiersTrouves) {
                System.out.println(extractRoot(fichier.getAbsolutePath()));
                //System.out.println();
            }
        }
    }

    private static String extractRoot(String pathString) {
        Path path = FileSystems.getDefault().getPath(pathString);

        // Trouver l'index du nom d'utilisateur dans le chemin
        int usernameIndex = -1;
        for (int i = 0; i < path.getNameCount(); i++) {
            if (path.getName(i).toString().equals("root")) {
                usernameIndex = i;
                break;
            }
        }

        // Construire la nouvelle chaîne à partir du nom d'utilisateur
        if (usernameIndex != -1 && usernameIndex < path.getNameCount() - 1) {
            StringBuilder result = new StringBuilder();
            for (int i = usernameIndex; i < path.getNameCount(); i++) {
                if (i > usernameIndex) {
                    result.append(FileSystems.getDefault().getSeparator());
                }
                result.append(path.getName(i));
            }
            return result.toString();
        } else {
            return "root";
        }
    }

    private static List<File> rechercherFichier(File repertoire, String nomFichier) {
        List<File> fichiersTrouves = new ArrayList<>();

        if (repertoire.isDirectory()) {
            File[] contenu = repertoire.listFiles();
            if (contenu != null) {
                for (File element : contenu) {
                    if (element.isFile() && element.getName().equals(nomFichier)) {
                        fichiersTrouves.add(element);
                    } else if (element.isDirectory()) {
                        // Récursivement recherche dans les sous-répertoires
                        fichiersTrouves.addAll(rechercherFichier(element, nomFichier));
                    }
                }
            }
        }

        return fichiersTrouves;
    }

    private static void remonterDossierParent() {
        File repertoireFile = new File(racineRelative);
        File dossierParent = repertoireFile.getParentFile();

        if (dossierParent != null && !Objects.equals(racine, dossierParent.getAbsolutePath())) {
            racineRelative = dossierParent.getAbsolutePath();
            System.out.println("Vous avez change de répertoire");
        } else {
            System.out.println("Vous êtes déjà dans le répertoire racine.");
        }
    }

    private static void ajouterTexteAnnotation(String ner, String texte) {
        if (ner != null && texte != null) {
            try {
                int nerIndex = Integer.parseInt(ner);
                File repertoireFile = new File(racineRelative);
                File[] contenu = repertoireFile.listFiles();

                if (contenu != null && nerIndex >= 0 && nerIndex <= contenu.length) {
                    File fichierSelectionne = contenu[nerIndex];
                    String nomFichier = fichierSelectionne.getName();
                    String annotation = "";
                    // Mettons à jour le fichier d'index avec le nouvel élément collé
                    List<String> lignesIndex = lireFichierIndex();

                    for (int i = 0; i < lignesIndex.size(); i++) {
                        String[] elements = lignesIndex.get(i).split(",");
                        if (elements[1].equals(nomFichier)) {
                            if (elements.length == 3) {
                                annotation = elements[2];
                            }
                            lignesIndex.set(i, elements[0]
                                    + "," + elements[1]
                                    + "," + annotation
                                    + texte);
                            ecrireFichierIndex(lignesIndex);
                            System.out.println("Texte ajouté à l'annotation avec succès !");
                            return;
                        }
                    }

                    System.out.println("Le fichier sélectionné n'a "
                            + "pas d'entrée dans le fichier d'index.");
                } else {
                    System.out.println("Numéro associé invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("NER doit être un nombre entier. Réessayez.");
            }
        } else {
            System.out.println("NER ou texte non spécifié. Réessayez.");
        }
    }

    private static void retirerTexteAnnotation(String ner) {
        if (ner != null) {
            try {
                int nerIndex = Integer.parseInt(ner);
                File repertoireFile = new File(racineRelative);
                File[] contenu = repertoireFile.listFiles();

                if (contenu != null && nerIndex >= 0 && nerIndex <= contenu.length) {
                    File fichierSelectionne = contenu[nerIndex];
                    String nomFichier = fichierSelectionne.getName();

                    // Mettons à jour le fichier d'index avec le nouvel élément collé
                    List<String> lignesIndex = lireFichierIndex();

                    for (int i = 0; i < lignesIndex.size(); i++) {
                        String[] elements = lignesIndex.get(i).split(",");
                        if (elements[1].equals(nomFichier)) {
                            lignesIndex.set(i, elements[0] + "," + elements[1] + ",");
                            //ecrireFichierIndex(lignesIndex);
                            mettreAjourFichierIndex(contenu, lignesIndex);
                            System.out.println("Texte retiré de l'annotation avec succès !");
                            //afficherContenuRepertoireCourant();
                            return;
                        }
                    }

                    System.out.println("Le fichier sélectionné n'a pas d'entrée "
                            + "dans le fichier d'index.");
                } else {
                    System.out.println("Numéro associé invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("NER doit être un nombre entier. Réessayez.");
            }
        } else {
            System.out.println("NER non spécifié. Réessayez.");
        }
    }

    private static void afficherAide() {
        System.out.println(" Commandes disponibles : ");
        System.out.println("ls ,                    Afficher le contenu du répertoire");
        System.out.println("pwd ,                   Afficher"
                + " la position actuelle dans l'arborescence");
        System.out.println(".. ,                    Naviguer dans le repertoire parent");
        System.out.println("find [<Arg>],           Naviguer dans le repertoire parent");
        System.out.println("mkdir [<Arg>],          Créer un repertoire");
        System.out.println("[<NER>],                Naviguer dans le répertoire");
        System.out.println("[<NER>] +,              Ajouter une annotation");
        System.out.println("[<NER>] -,              Retirer une annotation");
        System.out.println("[<NER>] cp,             Copier un fichier");
        System.out.println("[<NER>] cut,            Couper un fichier");
        System.out.println("past,                   Coller un fichier au"
                + " préalable copier ou couper");
        System.out.println("[<NER>] rm,             Supprimer un fichier");
        System.out.println("[<NER>] visu,           Visualiser un fichier");
        System.out.println("exit,                   Quitter le gestionnaire de fichiers");
        System.out.println("help,                   Afficher cette aide");
    }

    private static boolean estNumeric(String variable) {
        try {
            Integer.parseInt(variable);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void creeSousRepertoire(String nomSousRepertoire) {
        File sousRepertoire = new File(racineRelative + File.separator + nomSousRepertoire);

        if (!sousRepertoire.exists()) {
            if (sousRepertoire.mkdir()) {
                System.out.println("Sous-répertoire créé : " + extractRoot(sousRepertoire.getAbsolutePath()));

                // Créons le fichier d'index pour le sous-répertoire s'il n'existe pas
                File fichierIndex = new File(sousRepertoire.getAbsolutePath()
                        + File.separator + "notes.txt");
                if (!fichierIndex.exists()) {
                    try {
                        if (fichierIndex.createNewFile()) {
                            System.out.println("Fichier d'index créé pour le sous-répertoire : "
                                    + extractRoot(fichierIndex.getAbsolutePath()));
                        } else {
                            System.err.println("Impossible de créer le fichier"
                                    + " d'index pour le sous-répertoire : "
                                    + fichierIndex.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        System.err.println("Une erreur est survenu");
                    }
                }
            } else {
                System.err.println("Impossible de créer le sous-répertoire : "
                        + sousRepertoire.getAbsolutePath());
            }
        } else {
            System.out.println("Le sous-répertoire existe déjà.");
        }
        //afficherContenuRepertoireCourant();
    }

    private static void visualiserFichier(String ner) {
        if (ner != null) {
            try {
                int nerIndex = Integer.parseInt(ner);
                File repertoireFile = new File(racineRelative);
                File[] contenu = repertoireFile.listFiles();

                if (contenu != null && nerIndex >= 0 && nerIndex <= contenu.length) {
                    File fichierSelectionne = contenu[nerIndex];
                    if (fichierSelectionne.isFile()) {
                        if (isFichierTexte(fichierSelectionne)) {
                            afficherContenuFichierTexte(fichierSelectionne);
                        } else {
                            afficherTailleFichier(fichierSelectionne);
                        }
                    } else {
                        System.out.println("Le numéro associé doit pointer vers "
                                + "un fichier et non un repertoire.");
                    }
                } else {
                    System.out.println("Numéro associé invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("NER doit être un nombre entier. Réessayez.");
            }
        } else {
            System.out.println("NER non spécifié. Réessayez.");
        }
    }

    private static boolean isFichierTexte(File fichier) {
        String nomFichier = fichier.getName();
        return nomFichier.toLowerCase().endsWith(".txt");
    }

    private static void afficherContenuFichierTexte(File fichier) {
        System.out.println("Contenu du fichier texte : " + fichier.getName());
        try (BufferedReader reader = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                System.out.println(ligne);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }

    private static void afficherTailleFichier(File fichier) {
        System.out.println("Taille du fichier : " + fichier.length() + " octets");
    }

}