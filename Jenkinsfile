def pipeline
node('master') {
	checkout scm
    git url: 'https://github.com/riXab/groovy-pipeline-scripting.git'
    pipeline = load 'pipeline.groovy'
    pipeline.devQAStaging()
}
pipeline.production()