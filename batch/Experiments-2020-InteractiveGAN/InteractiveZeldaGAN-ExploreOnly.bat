cd ..
cd ..
java -jar target/MM-NEAT-0.0.1-SNAPSHOT.jar runNumber:%1 randomSeed:%1 trials:1 mu:20 maxGens:500 io:true netio:true GANInputSize:10 mating:true fs:false task:edu.southwestern.tasks.interactive.gvgai.ZeldaGANLevelBreederTask genotype:edu.southwestern.evolution.genotypes.BoundedRealValuedGenotype watch:false cleanFrequency:-1 simplifiedInteractiveInterface:false saveAllChampions:true cleanOldNetworks:false ea:edu.southwestern.evolution.selectiveBreeding.SelectiveBreedingEA imageSize:200 zeldaGANModel:ZeldaDungeonsAll3Tiles_10000_10.pth zeldaGANUsesOriginalEncoding:false allowInteractiveEvolution:false showInteractiveGANModelLoader:false base:interactivezeldagan saveTo:Explore log:InteractiveZeldaGAN-Explore cleanOldNetworks:false