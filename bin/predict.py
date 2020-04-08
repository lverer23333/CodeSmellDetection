import importlib,sys
importlib.reload(sys)
import numpy as np
import time
import os
np.random.seed(1337)

from keras.preprocessing.text import Tokenizer
from keras.preprocessing.sequence import pad_sequences
from keras.models import model_from_json  


if __name__ == '__main__':

    MAX_SEQUENCE_LENGTH = 15

    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

    # 用来测试的项目路径
    TESTPATH = 'C:/Users/Administrator/git/FeatrueEnvyPlugin/src/data/'
    # 用来测试的模型
    MODELPATH = 'C:/Users/Administrator/git/FeatrueEnvyPlugin/src/'
    # methodId文件
    FILENAME = 'C:/Users/Administrator/git/FeatrueEnvyPlugin/src/data/id.txt'
    # 预测结果
    TARGETPATH = 'C:/Users/Administrator/git/FeatrueEnvyPlugin/src/data/predictResult.txt'
    values = []
    methodIds = []
    print ("start time:"+time.strftime("%Y/%m/%d  %H:%M:%S"))
    start = time.clock()

    if (os.path.exists(TARGETPATH)):
        os.remove(TARGETPATH)
    resultWriter = open(TARGETPATH, 'w')

    '''神经网络所需的传入信息：①当前方法的名字、当前方法所在类的名字，目标类的名字 ②当前方法到当前类的距离，当前方法到目标类的距离'''

    # 记录每一个为正（有坏味）的用例中，方法所对应的正确的目标类
    f = open(FILENAME, 'r', encoding = 'utf-8')
    for line in f:
        line = line.strip('\n')
        methodIds.append(line)

    model = model_from_json(open(MODELPATH + 'my_model.json').read())
    model.load_weights(MODELPATH + 'my_model_weights.h5')

    for methodId in methodIds:
        test_distances = []
        test_labels = []
        test_texts = []
        targetClassNames=[]

        if(os.path.exists(TESTPATH + methodId + '_distances.txt')):
            # 针对每个用例，每一行表示该方法到该类的距离，该方法到目标类的距离
            with open(TESTPATH + methodId + '_distances.txt','r') as file_to_read:
                for line in file_to_read.readlines():
                    values = line.split()
                    test_distance = values[:2]
                    test_distances.append(test_distance)

            # 针对每个用例，每一行表示该方法名字，该方法所在类名字，目标类名字
            with open(TESTPATH + methodId + '_names.txt','r') as file_to_read:
                for line in file_to_read.readlines():
                    test_texts.append(line)
                    line = line.split()

            # 使用Tokenizer建立基于词典位序的文本向量表示
            tokenizer1 = Tokenizer(num_words=None)
            tokenizer1.fit_on_texts(test_texts)
            test_sequences = tokenizer1.texts_to_sequences(test_texts)
            test_word_index = tokenizer1.word_index
            test_data = pad_sequences(test_sequences, maxlen=MAX_SEQUENCE_LENGTH)

            # 将结构数据转成asarray
            test_distances = np.asarray(test_distances)

            # 定义train set
            x_val = []
            x_val_names = test_data
            x_val_dis = test_distances
            x_val_dis = np.expand_dims(x_val_dis, axis=2)
            x_val.append(x_val_names)
            x_val.append(np.array(x_val_dis))

            # 进行预测
            result = 0
            preds = model.predict_classes(x_val) # 获取preds的值（1/有坏味或0/无坏味）
            preds_double = model.predict(x_val) # 获取精度为double的preds的值（为0的概率，为1的概率）
            for i in range(len(preds)):
                # preds[i]是第i个例子的预测结果，只要有一个预测出有坏味（NUM_ONE！=0），就是有坏味；全都是无坏味（NUM_ONE=0），才是无坏味
                if(preds[i]!=0):
                    result = 1    # 预测结果是正，有坏味
                    break

            # 写入文件，第一行：方法id->有无坏味标识（1或0）->有几个用例
            resultWriter.write(str(methodId) + " " + str(result) + " " + str(len(preds)) + "\n")

            # 写入文件，后面len(preds)行都是为1的概率
            for i in range(len(preds)):
                resultWriter.write(str(preds_double[i][1]) + "\n")

    print ("end time:"+time.strftime("%Y/%m/%d  %H:%M:%S"))

    resultWriter.close()
