package en.actionsofproject.database;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class PredictionModel{
	
    public PredictionModel(){
    	// 使用绝对路径加载模型
    	String path = "C:\\Users\\Administrator\\Desktop\\ReadMe\\Algorithm\\my_model_weights.pb";
        float[][] input = new float[1][100];

        for (int i=0; i < 100; i++){
            input[0][i] = (float) (Math.random() * 100);
        }

        try (Graph graph = new Graph()){
            graph.importGraphDef(Files.readAllBytes(Paths.get(path)));
            Session sess = new Session(graph);
            Tensor x = Tensor.create(input); 
            Tensor y = sess.runner().feed("input_1", x).fetch("output_1").run().get(0);

            float[] res = (float[]) y.copyTo(new float[1]);
            System.out.println(Arrays.toString(y.shape()));
            System.out.println(Arrays.toString(res));
            
            
        }catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}
