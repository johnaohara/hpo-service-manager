import React from 'react';
import {
    PageSection,
    Grid,
    GridItem,
    Breadcrumb,
    BreadcrumbItem,
    ProgressStepper,
    ProgressStep, Card, CardHeader, CardBody, Title
} from '@patternfly/react-core';

interface Experiment {
    name: string;
    trial: number;
}

type ExampleType = 'default' | 'compact' | 'compactBorderless';

export const NewExperimentWizard: React.FunctionComponent = () => {
    // In real usage, this data would come from some external source like an API via props.
    const experiement: Experiment = {name: 'techempower', trial: 1};

    const columnNames = {
        name: 'Name',
        trial: 'Progress',
        actions: 'Actions'
    };


    return (
        <React.Fragment>
            <PageSection>
                <Grid hasGutter>
                    <GridItem span={12}><BreadcrumbBasic/></GridItem>
                    <GridItem span={3}><NewExperiementWizzard/></GridItem>
                    <GridItem span={9}><ValidateConfiguration/></GridItem>

                </Grid>
            </PageSection>
        </React.Fragment>
    );
};

const BreadcrumbBasic: React.FunctionComponent = () => (
    <Breadcrumb>
        <BreadcrumbItem to="#">experiments</BreadcrumbItem>
        <BreadcrumbItem to="#" isActive>techempower</BreadcrumbItem>
    </Breadcrumb>
);

const NewExperiementWizzard = () => (
    <ProgressStepper isVertical>
        <ProgressStep
            variant="success"
            description="Upload experiment definition yaml"
            id="vertical-desc-step1"
            titleId="vertical-desc-step1-title"
            aria-label="completed step, step with success"
        >
            Upload Experiment Configuration
        </ProgressStep>
        <ProgressStep
            variant="info"
            isCurrent
            description="Validate configuration against lab environment"
            id="vertical-desc-step2"
            titleId="vertical-desc-step2-title"
            aria-label="step with info"
        >
            Validate
        </ProgressStep>
        <ProgressStep
            variant="pending"
            description="Start Experiment running"
            id="vertical-desc-step3"
            titleId="vertical-desc-step3-title"
            aria-label="pending step"
        >
            Starting Experiment
        </ProgressStep>
    </ProgressStepper>
)

const ValidateConfiguration = () => (

    <Card>
        <CardHeader><Title headingLevel={"h2"}>Validate</Title> </CardHeader>
        <CardBody>
            <ProgressStepper isCenterAligned>
                <ProgressStep
                    variant="success"
                    description="Checking definition yaml"
                    id="center-desc-step1"
                    titleId="center-desc-step1-title"
                    aria-label="completed step, step with success"
                >
                    Config File
                </ProgressStep>
                <ProgressStep
                    variant="success"
                    description="Checking Jenkins Job definition"
                    id="center-desc-step1"
                    titleId="center-desc-step1-title"
                    aria-label="completed step, step with success"
                >
                    Jenkins
                </ProgressStep>
                <ProgressStep
                    variant="success"
                    isCurrent
                    description="Checking Horreum job configuration"
                    id="center-desc-step2"
                    titleId="center-desc-step2-title"
                    aria-label="step with info"
                >
                    Horreum
                </ProgressStep>
                <ProgressStep
                    variant="info"
                    description="Checking HPO experiment configuration"
                    id="center-desc-step3"
                    titleId="center-desc-step3-title"
                    aria-label="pending step"
                >
                    HPO
                </ProgressStep>
            </ProgressStepper>
        </CardBody>
    </Card>
)