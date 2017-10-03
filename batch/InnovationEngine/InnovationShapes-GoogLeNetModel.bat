cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:%1 randomSeed:%1 base:innovationshapes mu:400 maxGens:2000000 io:true netio:true mating:true task:edu.southwestern.tasks.innovationengines.ShapeInnovationTask log:InnovationShapes-GoogLeNetModel saveTo:GoogLeNetModel allowMultipleFunctions:true ftype:0 netChangeActivationRate:0.3 cleanFrequency:400 recurrency:false logTWEANNData:false logMutationAndLineage:true ea:edu.southwestern.evolution.mapelites.MAPElites experiment:edu.southwestern.experiment.evolution.SteadyStateExperiment mapElitesBinLabels:edu.southwestern.tasks.innovationengines.ImageNetBinMapping fs:true imageNetModel:edu.southwestern.networks.dl4j.GoogLeNetWrapper genotype:edu.southwestern.evolution.genotypes.ShapeInnovationGenotype pictureInnovationSaveThreshold:1.1 includeSigmoidFunction:false includeFullSigmoidFunction:true includeTanhFunction:false includeIdFunction:true includeFullApproxFunction:false includeApproxFunction:false includeGaussFunction:false includeFullGaussFunction:true includeSineFunction:true includeSawtoothFunction:false includeFullSawtoothFunction:true includeAbsValFunction:false includeHalfLinearPiecewiseFunction:false includeStretchedTanhFunction:false includeReLUFunction:false includeSoftplusFunction:false includeLeakyReLUFunction:false includeTriangleWaveFunction:false includeSquareWaveFunction:false includeCosineFunction:true