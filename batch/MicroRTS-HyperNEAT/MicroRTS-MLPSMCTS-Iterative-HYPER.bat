cd ..
cd ..
java -jar dist/MM-NEATv2.jar runNumber:%1 randomSeed:%1 base:microRTS trials:3 maxGens:500 mu:10 io:true netio:true mating:true task:edu.utexas.cs.nn.tasks.microrts.MicroRTSTask cleanOldNetworks:true fs:false log:MLPSMCTS-Iterative-HYPER saveTo:MLPSMCTS-Iterative-HYPER watch:false microRTSAgent:micro.ai.mcts.mlps.MLPSMCTS microRTSEnemySequence:edu.utexas.cs.nn.tasks.microrts.iterativeevolution.CompetitiveEnemySequence microRTSMapSequence:edu.utexas.cs.nn.tasks.microrts.iterativeevolution.DecceleratingMapSequence microRTSEvaluationFunction:edu.utexas.cs.nn.tasks.microrts.evaluation.NN2DEvaluationFunction  microRTSFitnessFunction:edu.utexas.cs.nn.tasks.microrts.fitness.ProgressiveFitnessFunction hyperNEAT:true genotype:edu.utexas.cs.nn.evolution.genotypes.HyperNEATCPPNGenotype allowMultipleFunctions:true ftype:1 netChangeActivationRate:0.3