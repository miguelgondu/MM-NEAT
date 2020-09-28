REM Usage:   postMAPElitesWatch.bat <experiment directory> <log prefix> <run type> <run number> <number of trials per individual> <filename>
REM Example: postMAPElitesWatch.bat loderunnerlevels LodeRunnerLevels MAPConnectedGroundLadders 0 5 Connected[00-10]Ground[8-12]Ladders[8-12]-elite.xml
java -ea -jar "target/MM-NEAT-0.0.1-SNAPSHOT.jar" runNumber:%4 parallelEvaluations:false base:%1 log:%2-%3 saveTo:%3 trials:%5 mapElitesArchiveFile:%6 io:false netio:false onlyWatchPareto:true printFitness:true animateNetwork:false monitorInputs:true experiment:edu.southwestern.experiment.post.ExploreMAPElitesExperiment logLock:true watchLastBest:false monitorSubstrates:true showCPPN:true substrateGridSize:10 showWeights:true watch:true showNetworks:true inheritFitness:false marioLevelAgent:ch.idsia.ai.agents.human.HumanKeyboardAgent marioStuckTimeout:99999 smartLodeRunnerEnemies:false
