cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:%1 base:zentangle saveTo:RandomBW log:Zentangle-RandomBW randomSeed:%1 trials:1 mu:16 maxGens:50 io:false netio:false mating:true fs:false starkPicbreeder:true task:edu.southwestern.tasks.zentangle.ZentangleTask allowMultipleFunctions:true ftype:0 watch:false netChangeActivationRate:0.3 cleanFrequency:-1 simplifiedInteractiveInterface:false recurrency:false saveAllChampions:true cleanOldNetworks:false ea:edu.southwestern.evolution.nsga2.NSGA2 imageWidth:2000 imageHeight:2000 imageSize:200 includeFullSigmoidFunction:true includeFullGaussFunction:true includeCosineFunction:true includeGaussFunction:false includeIdFunction:true includeTriangleWaveFunction:false includeSquareWaveFunction:false includeFullSawtoothFunction:false includeSigmoidFunction:false includeAbsValFunction:false includeSawtoothFunction:false overrideImageSize:true imageWidth:500 imageHeight:500 blackAndWhitePicbreeder:true