def pipeline
node('master') {
	checkout scm
    git url: 'https://github.com/riXab/reference-groovy-pipeline.git'
    pipeline = load 'flow.groovy'
    pipeline.devQAStaging()
}
pipeline.production()