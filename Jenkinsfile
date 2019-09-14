def pipeline
node('master') {
	checkout scm
	echo "****Checkout"
    git url: 'https://github.com/riXab/reference-groovy-pipeline.git'
    echo "****load pipeline"
	pipeline = load 'flow.groovy'
    pipeline.devQAStaging()
}
pipeline.production()