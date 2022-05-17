import fetchival from "fetchival"
import React, {useCallback, useEffect, useState} from 'react';
import {
    PageSection,
    Grid,
    GridItem,
    Breadcrumb,
    BreadcrumbItem,
    ProgressStepper,
    ProgressStep, Card, CardHeader, CardBody, Title, FileUpload, Stack, StackItem, Button, Flex, FlexItem, Alert
} from '@patternfly/react-core';
import {AlertVariant} from "@patternfly/react-core/dist/esm/components/Alert/Alert";
import {Link} from "@storybook/router";
import {Experiments} from "@app/Experiments/Experiments";

interface Experiment {
    name: string;
    trial: number;
}

export function NewExperimentWizard() {
    // In real usage, this data would come from some external source like an API via props.
    const [stage, setStage] = React.useState(0);
    const incrementStage = () => {
        setStage(stage + 1)
    }

    return (
        <React.Fragment>
            <PageSection>
                <Grid hasGutter>
                    <GridItem span={12}><BreadcrumbBasic/></GridItem>
                    <GridItem span={3}><NewExperimentWizzard stage={stage} stageIncrement={incrementStage}/></GridItem>
                    <GridItem span={9}><ActionPanel stage={stage} stageIncrement={incrementStage}/></GridItem>

                </Grid>
            </PageSection>
        </React.Fragment>
    );
};

const BreadcrumbBasic: React.FunctionComponent = () => (
    <Breadcrumb>
        <BreadcrumbItem to="#">experiments</BreadcrumbItem>
        <BreadcrumbItem to="#" isActive>new experiment</BreadcrumbItem>
    </Breadcrumb>
);


const NewExperimentWizzard = ({stage, stageIncrement}) => {

    return (
        <ProgressStepper isVertical>
            <ProgressStep
                variant={(stage === 0 ? "info" : "success")}
                description="Upload experiment definition yaml"
                id="vertical-desc-step1"
                titleId="vertical-desc-step1-title"
                aria-label="completed step, step with success"
            >
                Upload Experiment Configuration
            </ProgressStep>
            <ProgressStep
                variant={(stage === 1 ? "info" : (stage > 1 ? "success" : "pending"))}
                isCurrent
                description="Validate configuration against lab environment"
                id="vertical-desc-step2"
                titleId="vertical-desc-step2-title"
                aria-label="step with info"
            >
                Validate
            </ProgressStep>
{/*            <ProgressStep
                variant={(stage === 2 ? "info" : (stage > 2 ? "success" : "pending"))}
                description="Start Experiment running in Performance Lab"
                id="vertical-desc-step3"
                titleId="vertical-desc-step3-title"
                aria-label="pending step"
            >
                Start Experiment
            </ProgressStep>*/}
        </ProgressStepper>
    )
}

const ActionPanel = ({stage, stageIncrement}) => {

    const [experimentName, setExperimentName] = React.useState('');

    return (
        <Stack>
            <StackItem hidden={!(stage === 0)}><UploadConfiguration stageIncrement={stageIncrement}
                                                                    setExperimentName={setExperimentName}/></StackItem>
            <StackItem hidden={!(stage === 1)}><ValidateConfiguration stageIncrement={stageIncrement}
                                                                      experimentName={experimentName}/></StackItem>
            {/*<StackItem hidden={!(stage === 3)}><StartExperiment /></StackItem>*/}
        </Stack>

    )
}

const UploadConfiguration = ({stageIncrement, setExperimentName}) => {
    return (
        <Card>
            <CardHeader><Title headingLevel={"h2"}>Upload Experiment Definition</Title> </CardHeader>
            <CardBody>
                <UploadExperimentDefinition stageIncrement={stageIncrement} setExperimentName={setExperimentName}/>
            </CardBody>
        </Card>


    )
}

var experiments = fetchival('/api/hpo/experiment')

const UploadExperimentDefinition = ({stageIncrement, setExperimentName}) => {
    const [value, setValue] = React.useState('');
    const [filename, setFilename] = React.useState('');
    const [isLoading, setIsLoading] = React.useState(false);
    const [isSending, setIsSending] = useState(false)
    const [err, setError] = useState('')

    const uploadExperiment = useCallback(async () => {
        // don't send again while we are sending
        if (isSending) return
        // update state
        setIsSending(true)
        // send the actual request

        let newExpRequest = new Request(
            "/api/hpo/experiment",
            {
                method: "post",
                headers: {'Content-Type': 'text/plain'},
                body: value
            }
        )


        let response = await fetch(newExpRequest);

        if (response.ok) {
            let data = await response.json();
            // console.log(data);
            setExperimentName(data["message"]);
            stageIncrement();
        } else {
            let data = await response.json();
            setError(data["message"]);
        }

        setIsSending(false)
    }, [isSending, value]) // update the callback if the state changes


    const handleFileInputChange = (event, file) => setFilename(file.name);
    const handleTextOrDataChange = value => setValue(value);
    const handleClear = event => {
        setFilename('');
        setValue('');
    }
    const handleFileReadStarted = fileHandle => setIsLoading(true);
    const handleFileReadFinished = fileHandle => setIsLoading(false);

    const createNewExperiment = event => {
        setError('');
        uploadExperiment();
    }

    // const { value, filename, isLoading } = this.state;
    return (
        <Stack hasGutter={true}>
            <StackItem isFilled>
                <FileUpload
                    id="text-file-with-edits-allowed"
                    type="text"
                    value={value}
                    filename={filename}
                    filenamePlaceholder="Drag and drop a file or upload one"
                    onFileInputChange={handleFileInputChange}
                    onDataChange={handleTextOrDataChange}
                    onClearClick={handleClear}
                    onTextChange={handleTextOrDataChange}
                    onReadStarted={handleFileReadStarted}
                    onReadFinished={handleFileReadFinished}
                    isLoading={isLoading}
                    allowEditingUploadedText={false}
                    browseButtonText="Upload"
                />
            </StackItem>
            <StackItem hidden={err === ''}>
                <Alert title={"Error parsing configuration"} variant={AlertVariant.danger} isInline>{err}</Alert>
            </StackItem>
            <StackItem>
                <Flex className="example-border">
                    <FlexItem align={{default: 'alignRight'}}><Button onClick={createNewExperiment}
                                                                      isDisabled={value === ''}>Create
                        Experiment</Button></FlexItem>
                </Flex>

            </StackItem>
        </Stack>

    )
}

function ValidateConfiguration({stageIncrement, experimentName}) {

    const [validationStage, setValidationStage] = React.useState(5);
    const [ticking, setTicking] = useState(false);

    const startExperiment = () => {
        // alert("Starting Experiment: " + experimentName);
        stageIncrement();


    }


    return (
        <Card>
            <CardHeader><Title headingLevel={"h2"}>Validating Experiment: {experimentName}</Title> </CardHeader>
            <CardBody>
                <Stack hasGutter>
                    <StackItem>
                        <ProgressStepper isCenterAligned>
                            <ProgressStep
                                variant={(validationStage === 0 ? "info" : "success")}
                                description="Checking definition yaml"
                                id="center-desc-step1"
                                titleId="center-desc-step1-title"
                                aria-label="completed step, step with success"
                            >
                                Config File
                            </ProgressStep>
                            <ProgressStep
                                variant={(validationStage === 1 ? "info" : (validationStage > 1 ? "success" : "pending"))}
                                description="Checking Jenkins Job definition"
                                id="center-desc-step1"
                                titleId="center-desc-step1-title"
                                aria-label="completed step, step with success"
                            >
                                Jenkins
                            </ProgressStep>
                            <ProgressStep
                                variant={(validationStage === 2 ? "info" : (validationStage > 2 ? "success" : "pending"))}
                                isCurrent
                                description="Checking Horreum job configuration"
                                id="center-desc-step2"
                                titleId="center-desc-step2-title"
                                aria-label="step with info"
                            >
                                Horreum
                            </ProgressStep>
                            <ProgressStep
                                variant={(validationStage === 3 ? "info" : (validationStage > 3 ? "success" : "pending"))}
                                description="Checking HPO experiment configuration"
                                id="center-desc-step3"
                                titleId="center-desc-step3-title"
                                aria-label="pending step"
                            >
                                HPO
                            </ProgressStep>
                        </ProgressStepper>
                    </StackItem>
                    <StackItem>
                        <Flex>
                            <FlexItem align={{default: 'alignRight'}}>
                                {/*<Button disabled={true} onClick={Experiments}>Start Experiment</Button>*/}

                                {/*<Button variant="link" component={(props: any) => <Link {...props} to="/" />}>*/}
                                {/*    Router link*/}
                                {/*</Button>*/}
                            </FlexItem>
                        </Flex>
                    </StackItem>
                </Stack>
            </CardBody>
        </Card>
    )
}

const StartExperiment = () => {
    return (

        <Link to={'/'}>Experiments</Link>
    )
}