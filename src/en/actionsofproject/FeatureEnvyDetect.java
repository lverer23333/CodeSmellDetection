package en.actionsofproject;

import en.actionsofproject.database.ActionsAboutDB;

public class FeatureEnvyDetect {
	public FeatureEnvyDetect() {
		ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
		
		
		try {
			actionsAboutDB.inputdataPreprocessing();
			actionsAboutDB.predict();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
