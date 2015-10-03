
withConfig(configuration) {
    inline(phase: 'CONVERSION') { source, context, classNode ->
        classNode.putNodeMetaData('projectVersion', '3.0.7')
        classNode.putNodeMetaData('projectName', 'asset-pipeline')
        classNode.putNodeMetaData('isPlugin', 'true')
    }
}
