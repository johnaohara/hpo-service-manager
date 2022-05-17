import React from 'react';
import {
    PageSection,
    Grid,
    GridItem,
    Breadcrumb,
    BreadcrumbItem,
    Title,
    Card,
    CardHeader,
    CardBody,
    Split,
    SplitItem,
    Button,
    Stack, StackItem
} from '@patternfly/react-core';
import {ChartDonut} from '@patternfly/react-charts';
import {TableComposable, Thead, Tr, Th, Tbody, Td, ExpandableRowContent} from '@patternfly/react-table';
import {TrialHistory} from "@app/Experiments/details/TrialHistory";
import {Tabs, Tab, TabTitleText, Tooltip} from '@patternfly/react-core';
import {useParams} from "react-router";
import {Rect} from "victory-core";

interface ExperimentDetails {
    name: string;
    total_trials: number;
    current_trial: number;

    // public Map<Integer, TrialResultDAO> trialHistory;
}

export const ExperimentDetails = () => {
    // const { name } = useParams();
    // @ts-ignore
    const {name} = useParams();

    return (
        <React.Fragment>
            <PageSection>
                <Grid hasGutter>
                    <GridItem span={12}><BreadcrumbBasic experimentName={name}/></GridItem>
                    <GridItem span={12}><ExperimentTabs experimentName={name}/></GridItem>

                </Grid>
            </PageSection>
        </React.Fragment>
    );
};

function BreadcrumbBasic({experimentName}) {

    return (
        <Card>
            <CardHeader> <Breadcrumb>
                <Breadcrumb>
                    <BreadcrumbItem to="#">experiments</BreadcrumbItem>
                    <BreadcrumbItem to="#" isActive>local-test</BreadcrumbItem>
                </Breadcrumb>
            </Breadcrumb>
            </CardHeader>

            <CardBody>

                <Split>
                    <SplitItem><Title headingLevel={"h2"}>{experimentName}</Title></SplitItem>
                    <SplitItem isFilled>&nbsp;</SplitItem>
                </Split>


            </CardBody>
        </Card>
    )
};


const TrialProgress = ({experiment}) => {
    return (
        <React.Fragment>
            <Title headingLevel={"h2"}>&nbsp;</Title>

            <div style={{height: '230px', width: '230px'}}>
                <ChartDonut
                    ariaDesc="Trial Progress"
                    ariaTitle="Trial Progress"
                    constrainToVisibleArea={true}
                    data={[{
                        x: 'Complete',
                        y: (experiment.current_trial === -1 ? experiment.total_trials : experiment.current_trial)
                    }, {
                        x: 'outstanding',
                        y: (experiment.current_trial === -1 ? 0 : (experiment.total_trials - experiment.current_trial))
                    }]}
                    labels={({datum}) => `${datum.x}: ${datum.y}%`}
                    subTitle="Trials"
                    title={(experiment.current_trial === -1 ? experiment.total_trials : experiment.current_trial)}
                />
            </div>
        </React.Fragment>
    )
}

const LastTenTrials = ({experiment}) => {


    //TODO: surely there is a better way to express this structure in TS!?!?
    interface TuneableConfig {
        name: string;
        value: string;
    }

    interface Tuneables {
        [index: number]: TuneableConfig
    }

    interface TrialDetails {
        id: number;
        value: number;
        config?: Tuneables;
    }


    const trials: TrialDetails[] = [
        {
            id: 58,
            value: 37.5,
            config: [
                {name: "memoryRequest", value: "15"},
                {name: "cpuRequest", value: "2.5"},
            ]
        },
        {
            id: 57,
            value: 90.00,
            config: [
                {name: "memoryRequest", value: "30.0"},
                {name: "cpuLimit", value: "3.0"},
            ]
        },
        {
            id: 56,
            value: 73.29,
            config: [
                {name: "memoryRequest", value: "24.43"},
                {name: "cpuLimit", value: "3.0"},
            ]
        },
        {
            id: 55,
            value: 68.16,
            config: [
                {name: "memoryRequest", value: "28.4"},
                {name: "cpuLimit", value: "2.40"},
            ]
        },
        {
            id: 54,
            value: 84.23,
            config: [
                {name: "memoryRequest", value: "30.0"},
                {name: "cpuLimit", value: "3.0"},
            ]
        },
    ];

    const columnNames = {
        id: 'ID',
        value: 'Value',
    };

    // In this example, expanded rows are tracked by the repo names from each row. This could be any unique identifier.
    // This is to prevent state from being based on row order index in case we later add sorting.
    // Note that this behavior is very similar to selection state.
    const initialExpandedTrialNumbers = []; //trials.filter(trial => !!trial.config).map(trial => trial.id); // Default to all expanded
    const [expandedTrialNumbers, setExpandedTrialNumbers] = React.useState<number[]>(initialExpandedTrialNumbers);
    const setRepoExpanded = (trial: TrialDetails, isExpanding = true) =>
        setExpandedTrialNumbers(prevExpanded => {
            const otherExpandedTrialNumbers = prevExpanded.filter(r => r !== trial.id);
            return isExpanding ? [...otherExpandedTrialNumbers, trial.id] : otherExpandedTrialNumbers;
        });
    const isRepoExpanded = (repo: TrialDetails) => expandedTrialNumbers.includes(repo.id);

    return (
        <React.Fragment>
            <h2>Latest 5 trials</h2>
            <TableComposable aria-label="Expandable table" variant={'compact'}>
                <Thead>
                    <Tr>
                        <Th/>
                        <Th>{columnNames.id}</Th>
                        <Th>{columnNames.value}</Th>
                    </Tr>
                </Thead>
                {trials.map((trial, rowIndex) => {
                    // Some arbitrary examples of how you could customize the child row based on your needs
                    let childIsFullWidth = false;
                    let childHasNoPadding = false;
                    let detail1Colspan = 1;

                    return (
                        <Tbody key={trial.id} isExpanded={isRepoExpanded(trial)}>
                            <Tr>
                                <Td
                                    expand={
                                        trial.config
                                            ? {
                                                rowIndex,
                                                isExpanded: false, //isRepoExpanded(trial),
                                                onToggle: () => setRepoExpanded(trial, !isRepoExpanded(trial))
                                            }
                                            : undefined
                                    }
                                />
                                <Td dataLabel={columnNames.id}>{trial.id}</Td>
                                <Td dataLabel={columnNames.value}>{trial.value}</Td>
                            </Tr>
                            {trial.config ? (
                                <Tr isExpanded={isRepoExpanded(trial)}>
                                    {!childIsFullWidth ? <Td/> : null}
                                    {/*{trial.config.length > 0 ? (*/}
                                    <Td dataLabel="Trial detail 1" noPadding={childHasNoPadding}
                                        colSpan={detail1Colspan}>
                                        <ExpandableRowContent>{+": " + trial.config[0].value}</ExpandableRowContent>
                                    </Td>
                                    {/*) : null}*/}
                                </Tr>
                            ) : null}
                        </Tbody>
                    );
                })}
            </TableComposable>
        </React.Fragment>
    );
}


interface IProps {
}

interface IState {
    activeTabKey?: number;
    isBox?: boolean;
}


const RecommendedConfiguration = ({experiment}) => {
    // console.log(experiment.recommendedConfig.tunables);
    return (
        <Stack hasGutter={true}>
            {/*<StackItem><Title headingLevel={"h2"}>Recommended Configuration</Title></StackItem>*/}
            <StackItem><Title headingLevel={"h2"}>Value: {experiment.recommendedConfig.value}</Title></StackItem>
            <StackItem>
                <TableComposable aria-label="Expandable table" variant={'compact'}>
                    <Thead>
                        <Tr>
                            <Th>Tuneable</Th>
                            <Th>Value</Th>
                        </Tr>
                    </Thead>

                    {experiment.recommendedConfig.tunables.map((tuneable, rowIndex) => {
                        return (
                            <Tbody key={rowIndex} >
                                <Tr>
                                    <Td dataLabel="Tuneable">{tuneable.tunable}</Td>
                                    <Td dataLabel="Value">{tuneable.value}</Td>
                                </Tr>
                            </Tbody>
                        );
                    })}

                </TableComposable>

            </StackItem>
        </Stack>

    );
}

const ExperimentTabs = ({experimentName}) => {
    const [activeTabKey, setActiveTabKey] = React.useState(0);
    const [isBox, setIsBox] = React.useState(false);
    const [experiment, setExperiment] = React.useState({
        name: "",
        total_trials: -1,
        current_trial: -1,
        trialHistory: [],
        recommendedConfig: {
            id : -1,
            value: -1,
            tunables: []
        }
    });

    // Toggle currently active tab
    const handleTabClick = (event, tabIndex): void => {
        setActiveTabKey(tabIndex);
    };

    const toggleBox = (checked): void => {
        setIsBox(checked)
    };

    // console.log(experimentName);

    let newExpRequest = new Request(
        "/api/hpo/experiment/" + experimentName + "/status",
        {
            method: "get",
        }
    )

    fetch(newExpRequest)
        .then(res => res.json())
        .then(res => {
            setExperiment(res);
        })

    // console.log(experiment)

    return (
        <Card>
            <CardBody>
                <Tabs activeKey={activeTabKey} onSelect={handleTabClick} isBox={isBox}
                      aria-label="Tabs in the default example">
                    <Tab eventKey={0} title={<TabTitleText>Status</TabTitleText>}>
                        <Grid hasGutter>
                            <GridItem span={3}><TrialProgress experiment={experiment}/></GridItem>
                            <GridItem span={9}><TrialHistory experiment={experiment}/></GridItem>
                        </Grid>
                    </Tab>
                    <Tab eventKey={1} title={<TabTitleText>Recommended Configuration</TabTitleText>}>
                        <Grid hasGutter>
                            {experiment.recommendedConfig != null &&
                             <GridItem span={12}><RecommendedConfiguration experiment={experiment}/></GridItem>
                            }
                        </Grid>
                    </Tab>
                </Tabs>
            </CardBody>
        </Card>
    );
}

