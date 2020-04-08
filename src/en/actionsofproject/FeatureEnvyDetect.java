package en.actionsofproject;

import en.actionsofproject.database.ActionsAboutDB;

public class FeatureEnvyDetect {
	public FeatureEnvyDetect() {
		String path = "C:\\Users\\Administrator\\Desktop\\ReadMe\\Algorithm\\my_model_weights.pb";
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
