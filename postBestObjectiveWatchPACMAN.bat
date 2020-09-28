REM Usage:   postBestObjectiveWatch.bat <experiment directory> <log prefix> <run type> <run number> <number of trials per individual>
REM Example: postBestObjectiveWatch.bat onelifeconflict OneLifeConflict OneModule 0 5
java -jar "target/MM-NEAT-0.0.1-SNAPSHOT.jar" runNumber:%4 parallelEvaluations:false base:%1 log:%2-%3 saveTo:%3 trials:%5 watch:true showNetworks:true io:false netio:false onlyWatchPareto:true printFitness:true animateNetwork:false monitorInputs:true experiment:edu.southwestern.experiment.post.ObjectiveBestNetworksExperiment logLock:true watchLastBest:true monitorSubstrates:true showCPPN:true showWeights:true pacmanLives:3 probabilityThreshold:0.125f drawGhostPredictions:false drawPillModel:false ghostPO:true
