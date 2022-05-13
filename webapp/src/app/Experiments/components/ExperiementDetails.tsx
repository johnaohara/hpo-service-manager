import ReactDOM from 'react-dom';
import React from 'react';
import {
    PageSection, Grid, GridItem, Breadcrumb, BreadcrumbItem, Title, Card, CardHeader, CardBody
} from '@patternfly/react-core';
import {ChartDonut} from '@patternfly/react-charts';
import {TableComposable, Thead, Tr, Th, Tbody, Td, ExpandableRowContent} from '@patternfly/react-table';
import {TrialHistory} from "@app/Experiments/details/TrialHistory";
import {Tabs, Tab, TabTitleText, Tooltip} from '@patternfly/react-core';

interface Experiment {
    name: string;
    trial: number;
}

type ExampleType = 'default' | 'compact' | 'compactBorderless';

export const ExperimentDetails: React.FunctionComponent = () => {
    // In real usage, this data would come from some external source like an API via props.
    const experiement: Experiment = {name: 'local-test', trial: 1};

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
                    <GridItem span={12}><ExperimentTabs/></GridItem>

                </Grid>
            </PageSection>
        </React.Fragment>
    );
};

const BreadcrumbBasic: React.FunctionComponent = () => (
    <Breadcrumb>
        <BreadcrumbItem to="#">experiments</BreadcrumbItem>
        <BreadcrumbItem to="#" isActive>local-test</BreadcrumbItem>
    </Breadcrumb>
);


const TrialProgress = () => (
    <div style={{height: '230px', width: '230px'}}>
        <ChartDonut
            ariaDesc="Trial Progress"
            ariaTitle="Trial Progress"
            constrainToVisibleArea={true}
            data={[{x: 'Complete', y: 58}, {x: 'outstanding', y: 42}]}
            labels={({datum}) => `${datum.x}: ${datum.y}%`}
            subTitle="Trials"
            title="58"
        />
    </div>
)

const LastTenTrials: React.FunctionComponent = () => {


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


class ExperimentTabs extends React.Component<IProps, IState> {
    constructor(props: IProps) {
        super(props);
        this.state = {
            activeTabKey: 0,
            isBox: false
        };

    }

    // Toggle currently active tab
    handleTabClick(event, tabIndex): void {
        this.setState({
            activeTabKey: tabIndex
        });
    };

    toggleBox(checked): void {
        this.setState({
            isBox: checked
        });
    };

    render() {
        const {activeTabKey, isBox} = this.state;
        const tooltip = (
            <Tooltip content="Aria-disabled tabs are like disabled tabs, but focusable. Allows for tooltip support."/>
        );

        return (
            <Card>
                <CardHeader><Title headingLevel={"h1"}>local-test</Title></CardHeader>

                <CardBody>
                    <Tabs activeKey={activeTabKey} onSelect={this.handleTabClick} isBox={isBox}
                          aria-label="Tabs in the default example">
                        <Tab eventKey={0} title={<TabTitleText>Status</TabTitleText>}>
                            <Grid hasGutter>
                                <GridItem span={3}><TrialProgress/></GridItem>
                                <GridItem span={6}><LastTenTrials/></GridItem>
                                <GridItem span={3}>Current config</GridItem>
                                <GridItem span={12}><TrialHistory/></GridItem>
                            </Grid>
                        </Tab>
                        <Tab eventKey={1} title={<TabTitleText>Configuration</TabTitleText>}>
                            Config
                        </Tab>
                    </Tabs>
                </CardBody>
            </Card>
        );
    }
}
