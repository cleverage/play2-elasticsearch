import indexing.Country;
import indexing.Player;
import indexing.Team;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.modules.elasticsearch.IndexManager;

import java.util.Date;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {

        Logger.info("Application has started");

        // Clean the index
        IndexManager.cleanIndex(app);

        initTeams();
    }

    private void initTeams() {
        // init Country
        Country espagne = new Country();
        espagne.name = "espagne";
        espagne.continent = "europe";

        Country france = new Country();
        france.name = "france";
        france.continent = "europe";

        // init Team
        Team barcelone = new Team();
        barcelone.name = "FC Barcelone";
        barcelone.country = espagne;
        barcelone.level = "Ligua";
        barcelone.dateCreate = new Date();
        loadPlayersBarcelone(barcelone);
        barcelone.index();


        Team madrid = new Team();
        madrid.name = "Real Madrid";
        madrid.country = espagne;
        madrid.level = "Ligua";
        madrid.dateCreate = new Date();
        loadPlayersMadrid(madrid);
        madrid.index();



        Team ol = new Team();
        ol.name = "Olympique Lyonnais";
        ol.country = france;
        ol.level = "Ligue 1";
        ol.dateCreate = new Date();
        ol.index();
    }

    private void loadPlayersMadrid(Team madrid) {
        Player casillas = new Player();
        casillas.name ="casillas";
        casillas.weight=79;
        casillas.position.add(Player.Position.GOALKEEPER.toString());

        Player marcelo = new Player();
        marcelo.name ="marcelo";
        marcelo.weight=73;
        marcelo.position.add(Player.Position.DEFENDER.toString());

        Player benzema = new Player();
        benzema.name ="benzema";
        benzema.weight=73;
        benzema.position.add(Player.Position.FORWARD.toString());

        Player ronaldo = new Player();
        ronaldo.name ="ronaldo";
        ronaldo.weight=73;
        ronaldo.position.add(Player.Position.FORWARD.toString());

        madrid.players.add(casillas);
        madrid.players.add(marcelo);
        madrid.players.add(benzema);
        madrid.players.add(ronaldo);
    }

    private void loadPlayersBarcelone(Team barcelone) {
        Player valdes = new Player();
        valdes.name ="valdes";
        valdes.weight=78;
        valdes.position.add(Player.Position.GOALKEEPER.toString());

        Player abidal = new Player();
        abidal.name ="abidal";
        abidal.weight=75;
        abidal.position.add(Player.Position.DEFENDER.toString());

        Player alves = new Player();
        alves.name ="alves";
        alves.weight=64;
        alves.position.add(Player.Position.DEFENDER.toString());

        Player puyol = new Player();
        puyol.name ="puyol";
        puyol.weight=80;
        puyol.position.add(Player.Position.DEFENDER.toString());

        Player pique = new Player();
        pique.name ="pique";
        pique.weight=75;
        pique.position.add(Player.Position.DEFENDER.toString());

        Player xavi = new Player();
        xavi.name ="xavi";
        xavi.weight=68;
        xavi.position.add(Player.Position.MIDFIELDER.toString());

        Player inesta = new Player();
        inesta.name ="inesta";
        inesta.weight=64;
        inesta.position.add(Player.Position.MIDFIELDER.toString());

        Player fabregas = new Player();
        fabregas.name ="fabregas";
        fabregas.weight=69;

        Player messi = new Player();
        messi.name ="messi";
        messi.weight=67;
        messi.position.add(Player.Position.FORWARD.toString());
        messi.position.add(Player.Position.MIDFIELDER.toString());

        Player vila = new Player();
        vila.name ="vila";
        vila.weight=69;
        vila.position.add(Player.Position.FORWARD.toString());

        barcelone.players.add(valdes);
        barcelone.players.add(abidal);
        barcelone.players.add(alves);
        barcelone.players.add(puyol);
        barcelone.players.add(pique);
        barcelone.players.add(xavi);
        barcelone.players.add(inesta);
        barcelone.players.add(fabregas);
        barcelone.players.add(messi);
        barcelone.players.add(vila);
    }

    @Override
    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

}